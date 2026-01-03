
import test.BdecoderTest;
import test.BencoderTest;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //testBencoderTest();
        //testBdecodeTest();
    }

    public static void testBencoderTest(){
        BencoderTest bencoderTest = new BencoderTest();

        String announce = " http://tracker.example.com";
        String filePath = "testFile.txt";
        String encodedFilePath = "encoded";

        bencoderTest.runBencoderTest(announce, filePath, encodedFilePath);
    }

    public static void testBdecodeTest(){
        BdecoderTest bdecoderTest = new BdecoderTest();

        String torrentFilePath = "encoded.torrent";
        String decodedFilePath = "decoded.txt";

        bdecoderTest.runBdecoderTest(torrentFilePath, decodedFilePath);
    }
}