System wide
  * Documentation
  * Test coverage (PHPUnit)
  * Client Verification by API Token -- Implemented. See api-token branch. 
    Use salted SHA1 hash of ApiToken in POST-Content.
    The Json Data send to the http datacollector has to provide the key 'api-token' with the value hex(sha-1([api-token][salt])':'[salt])
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
