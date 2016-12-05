System wide
  * Documentation
  * Test coverage (PHPUnit)
  * Http data transfer compression (gzip Encoding)
    
    Implemented using gzip as content encoding header.
    
    To Do: Improve it by using the mime type application/json+zip and register a Slim media type parser on it.
    
  * Client Verification by API Token -- Implemented. See api-token branch. 
  
    The Json data send to the http datacollector has to provide the key 'api-token' with the value hex(sha-1([api-token][salt])':'[salt])
    
    Risk: If the traffic was intercepted, submitted tokens can be reused.
    
    Possible solutions: 
      * Make changing the salt with every request mandatory.
      * include a frequently changing information into the hash. Possible candidates: time, counters
  * support multitenancy

Monitor App:
  * DataCollector should perist data on disk, and load it on program initialization.
  * Add file based AppConfig implementation
  * Configurable Scheduler Timings (instead of hard coded ones)

Backend
  * Data: -> Sanity Check + Validation
  * Test support for MySQL databases (currently sqlite is used)

Frontend
  * UI Design
  * backend access
  * store settings in URL for bookmarking
