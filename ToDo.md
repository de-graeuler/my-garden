System wide
  * Documentation
  * Test coverage
  * Client Verification by API Token
    Use salted SHA1 hash of ApiToken in POST-URL
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
