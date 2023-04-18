package com.jingweizhang.dynaquery.web;

import com.jingweizhang.dynaquery.dto.DynaQueryReference;
import com.jingweizhang.dynaquery.dto.DynaQueryRequest;
import com.jingweizhang.dynaquery.service.DynaQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * A generic controller to handle all dynaquery request as long as the view entity is defined.
 *
 * @Author rocky.zhang on 2023/4/7
 */

@RestController
@RequestMapping("/dynaquery")
public class DynaQueryController {
    private final DynaQueryService dynaQueryService;
    public DynaQueryController(DynaQueryService dynaQueryService) {
        this.dynaQueryService = dynaQueryService;
    }

    @PostMapping(value = "/queryOne", produces = "application/json")
    @ResponseBody
    public Optional<Map<String, Object>> queryOne(@RequestBody DynaQueryRequest queryRequest) {
        return this.dynaQueryService.queryOne(queryRequest);
    }

    @PostMapping(value = "/queryAll/pageNumber/{pageNum}/pageSize/{pageSize}", produces = "application/json")
    @ResponseBody
    public Page<Map<String, Object>> queryAll(@RequestBody DynaQueryRequest queryRequest,
                                           @PathVariable int pageNum,
                                           @PathVariable int pageSize) {
        Pageable pageable;
        try {
            pageable = PageRequest.of(pageNum, pageSize);
        } catch (Exception e) {
            pageable = Pageable.unpaged();
        }
        return this.dynaQueryService.queryAll(queryRequest, pageable);
    }

    @PostMapping(value = "/queryAll", produces = "application/json")
    @ResponseBody
    public List<Map<String, Object>> queryAll(@RequestBody DynaQueryRequest queryRequest) {
        return this.dynaQueryService.queryAll(queryRequest, Pageable.unpaged()).getContent();
    }

    @GetMapping(value = "/queryAll/query/{id}/pageNumber/{pageNum}/pageSize/{pageSize}", produces = "application/json")
    @ResponseBody
    public Page<Map<String, Object>> querySavedQuery(@PathVariable int id,
                                                     @PathVariable int pageNum,
                                                     @PathVariable int pageSize) {
        Pageable pageable;
        try {
            pageable = PageRequest.of(pageNum, pageSize);
        } catch (Exception e) {
            pageable = Pageable.unpaged();
        }
        return this.dynaQueryService.querySavedQuery(id, pageable);
    }

    @GetMapping(value = "/queryAll/query/{id}", produces = "application/json")
    @ResponseBody
    public List<Map<String, Object>> querySavedQuery(@PathVariable int id) {
        return this.dynaQueryService.querySavedQuery(id, Pageable.unpaged()).getContent();
    }

    @PostMapping("/saveQuery/{name}/isDefault/{isDefault}")
    public DynaQueryReference saveQuery(@RequestBody DynaQueryRequest queryRequest,
                                        @PathVariable String name,
                                        @PathVariable boolean isDefault) {
        return this.dynaQueryService.saveQuery(queryRequest, name, isDefault);
    }

    @GetMapping("/queryReferences")
    public List<DynaQueryReference> getQueryReferences() {
        return this.dynaQueryService.listDynaQueryReferences();
    }
}
