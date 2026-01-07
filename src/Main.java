

import test.TestGenrateTorrent;
import test.TestSeeding;


public class Main {
    public static void main(String[] args) throws Exception {


        //토렌트 파일 생성
//        TestGenrateTorrent test = new TestGenrateTorrent();
//        test.runTestGenerateTorrent();
//

        //토렌트 파일 시딩
        TestSeeding test = new TestSeeding();
        test.runTestSeeding();

    }



}