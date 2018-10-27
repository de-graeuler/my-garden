<?php

namespace Graeuler\Garden\Collect;

interface DataSetInterface 
{

    /**
     * @param $mixed... open List of parameters that should be stored to the dataset.
     **/
    public function addValue($mixed);

    public function asIterableList();

}
