# Report Usage API Documentation

This documentation provides details about the REST API endpoints provided by the
Report Application

## Base URL
The base URL for accessing the API endpoints is: `/report`

## Endpoints

### 1. Get Monthly Summary

#### Endpoint
GET /monthly-summary

Retrieves the monthly summary report.

#### Parameters
- `year` (optional): Filter the results by the year.
- `month` (optional): Filter the results by the month.
- `applicationId` (optional): Filter the results by the application ID.
- `apiId` (optional): Filter the results by the API ID.
- `username` (optional): Filter the results by the username.
- `search` (optional): Perform a search on the results.
- `page` (optional, default: 0): The page number for paginated results.
- `size` (optional, default: 10): The number of results per page.

### 2. Get Monthly Summary Details

#### Endpoint
GET /monthly-summary/details

Retrieves the detailed monthly summary report.

#### Parameters
- `applicationId` (required): Filter the results by the application ID.
- `apiId` (required): Filter the results by the API ID.
- `search` (optional): Perform a search on the results.
- `page` (optional, default: 0): The page number for paginated results.
- `size` (optional, default: 10): The number of results per page.
- `username` (optional): Filter the results by the username.

### 3. Get Resource Summary

#### Endpoint
GET /resource-summary

Retrieves the resource summary report.

#### Parameters
- `year` (optional): Filter the results by the year.
- `month` (optional): Filter the results by the month.
- `resource` (optional): Filter the results by the resource.
- `apiId` (optional): Filter the results by the API ID.
- `username` (required): Filter the results by the username.
- `search` (optional): Perform a search on the results.
- `page` (optional, default: 0): The page number for paginated results.
- `size` (optional, default: 10): The number of results per page.

### 4. Get Resource Summary Details

#### Endpoint

GET /resource-summary/details

Retrieves the detailed resource summary report.

#### Parameters
- `resource` (required): Filter the results by the resource.
- `apiId` (required): Filter the results by the API ID.
- `search` (optional): Perform a search on the results.
- `page` (optional, default: 0): The page number for paginated results.
- `size` (optional, default: 10): The number of results per page.
- `username` (required): Filter the results by the username.




