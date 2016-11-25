@echo off
curl http://localhost:8081/datafeed/collect/garden ^
 -H "Content-Type: application/json" ^
 -d @testdata.json
