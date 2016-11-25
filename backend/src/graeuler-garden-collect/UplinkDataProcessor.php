<?php

namespace Graeuler\Garden\Collect;

class UplinkDataProcessor implements DataProcessor 
{
    
    private function fail($message) {
        throw new InvalidDataException('Data Processing Error: ' . $message);
    }
       
    // Interface function 
    public function buildDataSet(array $jsonData, $source = null) {
        if ( ! is_array($jsonData) ) $this->fail ('Given Data has to be an array.');
        if ( ! strtolower($source) === 'garden' ) $this->fail('Currently supports processing of garden data.');
        $dataSet = new GardenDataSet();
        foreach($jsonData as $key => $data) {
            if ( ! is_array($data) ) $this->fail(sprintf('Value for type %s has to be an array.', $key));
            foreach($data as $set) {
                if ( ! isset($set['t'] ) ) $this->fail (sprintf('IsoDateTime (t) missing in %s', $key));
                if ( ! isset($set['v'] ) ) $this->fail (sprintf('Value (v) missing in %s', $key));
                $dataSet->addValue($source, $key,  $set['t'], $set['v']);
            }
        }
        return $dataSet;
    }
        
}
