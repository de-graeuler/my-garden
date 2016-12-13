<?php

namespace Graeuler\Garden\Collect;

require_once 'DataFeedDatastoreTestBase.php';

class DataFeedDatastoreMySqlTest extends DataFeedDatastoreTestBase
{
    const DB_SETUP_SCRIPT = 'res/database-schema-mysql.sql'; //relative to phpunit execution. 
    
    public static function setUpBeforeClass() {
        self::$db = new \PDO('mysql:host=localhost;dbname=datafeed', 'datafeed', 'datafeed');
        /** setup should not be required. Test fails if database is not set up correctly 
        $setup = file_get_contents(self::DB_SETUP_SCRIPT);
        $q = self::$db->exec($setup);
         **/
    }
    
}