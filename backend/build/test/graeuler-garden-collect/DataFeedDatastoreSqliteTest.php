<?php

namespace Graeuler\Garden\Collect;

require_once 'DataFeedDatastoreTestBase.php';

class DataFeedDatastoreSqliteTest extends DataFeedDatastoreTestBase
{
    const DB_SETUP_SCRIPT = 'res/database-schema.sql'; //relative to phpunit execution. 
    
    public static function setUpBeforeClass() {
        self::$db = new \PDO('sqlite::memory:');
        $setup = file_get_contents(self::DB_SETUP_SCRIPT);
        $q = self::$db->exec($setup);
    }
    
}