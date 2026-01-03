
import test.BencoderTest;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        testBencoderTest();

    }

    public static void testBencoderTest(){
        BencoderTest bencoderTest = new BencoderTest();

        String announce = " http://tracker.example.com";
        String filePath = "encodeFile.txt";

        bencoderTest.runBencoderTest(announce, filePath);
    }

}