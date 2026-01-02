import bencode.BencodeTest;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        bencoderTest();

    }

    public static void bencoderTest(){
        Scanner scanner = new Scanner(System.in);
        BencodeTest tester = new BencodeTest();

        System.out.print("Bencode로 인코딩할 문자열을 입력하세요: ");
        String input = scanner.nextLine();

        // 1. 입력받은 문자열로 인코딩 테스트 수행
        tester.runBencodeTest(input);

        // 2. 디코딩 테스트 수행 및 결과 확인
        tester.runBdecodeTest();

        scanner.close();
    }
    
}