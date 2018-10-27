<?php

namespace Graeuler\Garden\Collect;

use PHPUnit\Framework\TestCase;

class GardenDataSetTest extends TestCase
{
    
    public function testNullInput() {
        $ds = new GardenDataSet();
        $ds->addValue(null);
        $r = $ds->asIterableList();
        $this->assertEmpty($r, print_r($r, true));
    }
    
    public function testFewValuesInput() {
        $ds = new GardenDataSet();
        $ds->addValue("val1", "val2");
        $r = $ds->asIterableList();
        $this->assertEmpty($r, print_r($r, true));
    }
    
    public function testManyValuesInput() {
        $ds = new GardenDataSet();
        $ds->addValue("val1", "val2", "val3", "val4", "val6", "val7");
        $r = $ds->asIterableList();
        $this->assertEmpty($r, print_r($r, true));
    }
    
    public function testGardenDataInput() {
        $ds = new GardenDataSet();
        $ds->addValue("source", "key", "isodatetime", "value");
        $r = $ds->asIterableList();
        $this->assertCount(1, $r);
    }
    
    private $records = array();
    
    /**
     * @param mixed - source, key, isodatetime, value
     **/
    public function addValue($mixed) { // in PHP 5.6 we should refactor this to ...$mixed
        $this->records[] = func_get_args();
    }
 
    public function asIterableList() {
        return $this->records;
    }    
 
}
