<?php

namespace Graeuler\Garden\Collect;

class GardenDataSet implements DataSetInterface 
{
    
    private $records = array();
    
    /**
     * @param mixed - source, key, isodatetime, value
     **/
    public function addValue($mixed) { // in PHP 5.6 we should refactor this to ...$mixed
        $args = func_get_args();
        if (sizeof($args) == 4) 
            $this->records[] = $args;
    }
 
    public function asIterableList() {
        return $this->records;
    }    
 
}
