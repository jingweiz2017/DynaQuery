# What is DynaQuery

This project is still under construction. Please check back later.

DynaQuery is a little tool to provide a way for the frontend to compose a query on the fly using JSON, and send it to backend for execution.
This enabled the frontend with an ability to allow user to compose a query on views exposed by the backend for any customized query user may have at runtime. In this case, only the data view is defined at design-time. How user want to operate the data in the view like filtering, aggregating, or sorting is up to the user to decide at run-time.

## How it works
DynaQuery is a thin layer on top of Spring Data Jpa Criteria API. It provides a way to convert a JSON query to a JPA Criteria Query. The JSON query is composed of a list of filters, and aggregations and sorts. The filters are used to filter the data, and the aggregations are used to aggregate the data, etc. The JSON query will be converted to a JPA Criteria Query, and then executed against the view build at design-time to get the result.

## How to use it
1. Define a view entity.\s\s It would be a POJO with JPA @Entity annotation attached and implement a marker interface(ViewEntity). This newly defined Entity can be mapped to a Table or a View in database.
   ![Headers](/screenshots/define-view-entity.png?raw=true)
2. Put all your view entities in a package.\s\s And through configuration file, specify the package name to DynaQuery. DynaQuery will scan the package and find all the view entities that marked as ViewEntity.
   ![Headers](/screenshots/view-entity-scan-config.png?raw=true)
3. Refer to swagger api `http://localhost:8080/swagger-ui/index.html` for how to compose a JSON query and send it to web controller for execution. 
   ![Headers](/screenshots/swagger-api.png?raw=true)
4. As you have already seen, use those generic query apis for singular query or plural query with pagination.

## Best Practice
1. A database view is always recommended to be created before a view entity is created to map to. 
   DynaQuery works on the view that user interact with. Any field that user will operate on like filtering, sorting, or aggregating should be a concrete field defined in the view entity. If this field is a result of calculation which doesn't have table column to support with, then the handling logic should be in database view. To the eye of DynaQuery, it will treat underlie view entity as a table.
