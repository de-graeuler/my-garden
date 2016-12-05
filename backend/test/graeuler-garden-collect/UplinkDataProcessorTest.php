<?php

namespace Graeuler\Garden\Collect;

use PHPUnit\Framework\TestCase;

class UplinkDataProcessorTest extends TestCase
{
    private $data = array(
        "api-token" => "token-not-evaluated-by-data-processor",
        "key1" => array(
            array(
                "t" => "2016-12-02T01:42:27.283+01:00[Europe\/Berlin]",
                "v" => 204.09
                ),
            array(
                "t" => "2016-12-02T01:45:27.283+01:00[Europe\/Berlin]",
                "v" => 201.09
                ),
            array(
                "t" => "2016-12-02T01:47:27.283+01:00[Europe\/Berlin]",
                "v" => 208.09
                )
            ),
        "key2" => array(
            array(
                "t" => "2016-12-02T01:42:27.283+01:00[Europe\/Berlin]",
                "v" => 204.09
                ),
            array(
                "t" => "2016-12-02T01:45:27.283+01:00[Europe\/Berlin]",
                "v" => 201.09
                ),
            array(
                "t" => "2016-12-02T01:47:27.283+01:00[Europe\/Berlin]",
                "v" => 208.09
                )
            )
        );
    private $bogusDataNoT = array(
        "api-token" => "token-not-evaluated-by-data-processor",
        "key1" => array(
            array(
                "v" => 204.09
                )
            )
        );
    private $bogusDataNoV = array(
        "api-token" => "token-not-evaluated-by-data-processor",
        "key1" => array(
            array(
                "t" => "2016-12-02T01:47:27.283+01:00[Europe\/Berlin]"
                )
            )
        );
        
    private $bogusDataNoKeyArray = array(
        "api-token" => "token-not-evaluated-by-data-processor",
        "key1" => null
        );
        
        
    public function testBuildDataSetGoodCase() {
        $dataProcessor = new UplinkDataProcessor();
        $result = $dataProcessor->buildDataSet($this->data, 'garden');
        $this->assertInstanceOf(GardenDataSet::class, $result);
    }
    
    /**
     * @expectedException Graeuler\Garden\Collect\InvalidDataException
     */
    public function testBuildDataSetNotGarden() {
        $dataProcessor = new UplinkDataProcessor();
        $result = $dataProcessor->buildDataSet($this->data, "unknown source");
    }
    
    /**
     * @expectedException Graeuler\Garden\Collect\InvalidDataException
     */
    public function testBuildDataSetBogusDataNoT() {
        $dataProcessor = new UplinkDataProcessor();
        $result = $dataProcessor->buildDataSet($this->bogusDataNoT, "garden");
    }

    /**
     * @expectedException Graeuler\Garden\Collect\InvalidDataException
     */
    public function testBuildDataSetBogusDataNoNoKeyArray() {
        $dataProcessor = new UplinkDataProcessor();
        $result = $dataProcessor->buildDataSet($this->bogusDataNoKeyArray, "garden");
    }

    /**
     * @expectedException Graeuler\Garden\Collect\InvalidDataException
     */
    public function testBuildDataSetBogusDataNoV() {
        $dataProcessor = new UplinkDataProcessor();
        $result = $dataProcessor->buildDataSet($this->bogusDataNoV, "garden");
    }

}
