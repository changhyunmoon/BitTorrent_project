package torrent;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GenrateTorrent {



     //단일 또는 다중 파일을 위한 토렌트 생성 메소드

    public Map<String, Object> createTorrent(
            String torrentName, String trackerUrl, String fileDirectory, String createdBy,
            String comment, int pieceLength, boolean isPrivate, List<String> fileNames
    )throws Exception {
        long now = System.currentTimeMillis() / 1000;

        System.out.println("\n\n======= [torrent file create start] =======");
        System.out.println("torrentName (토렌트 파일의 이름) = " + torrentName +
                "\ntrackerUrl (트래커 주소) = " + trackerUrl +
                "\nfileDirectory (토렌트 파일이 저장되오 있는 디렉토리 이름) = " + fileDirectory +
                "\ncreated on (토렌트 파일 생성 일자. 리눅스 시계) = " + now +
                "\ncreatedBy (토렌트 파일 생성자) = " + createdBy +
                "\ncomment (생성자가 적어 놓은 설명) = " + comment +
                "\npieceLength (파일 조각의 크기) = " + pieceLength +
                "\nisPrivate (공개, 비공개) = " + isPrivate +
                "\nfileNames (공유하는 실제 파일 이름들) = " + fileNames);
        System.out.println("=====================================");

        Map<String, Object> torrent = new HashMap<>();

        // 1. 메타데이터 (Meta Data) 설정
        torrent.put("torrent name", torrentName);                       //토렌트 파일의 이름
        //밑에서 info hash 구현
        torrent.put("announce", trackerUrl);                            //트래커 주소

        // 2. Meta data
        torrent.put("file directory", fileDirectory);                   //토렌트 파일이 저장되어 있는 디렉토리 이름
        torrent.put("created on", now);   //토렌트 파일 생성 일자
        torrent.put("created by", createdBy);                           //토렌트 파일 생성자 정보
        torrent.put("comment", comment);                                //토렌트 파일 생성자가 적어 놓은 설명
        torrent.put("piece length", pieceLength);                       //공유에 사용할 파일 조각의 크기
        torrent.put("file names", fileNames);                           //공유하는 실제 파일 이름들

        //info hash 계산
        byte[] infoHash = calInfoHash(fileDirectory, pieceLength, isPrivate, fileNames);
        torrent.put("info hash", infoHash);

        return torrent;
    }

    //infoHash 생성 함수
    //사용되는 변수들 : fileDirectory, pieceLength, fileNames
    public byte[] calInfoHash(
            String fileDirectory,
            int pieceLength,
            boolean isPrivate,
            List<String> fileNames) throws Exception{
        System.out.println("\n\n======= [info hash calculate start] =======");
        //info 딕셔너리 구성
        Map<String, Object> info = new HashMap<>();
        info.put("file directory", fileDirectory);
        info.put("piece length", pieceLength);
        info.put("is private", isPrivate);
        info.put("file names", fileNames);
        System.out.println("Info Map 구성 완료");
        System.out.println("file directory : " + fileDirectory);
        System.out.println("piece length : " + pieceLength);
        System.out.println("is private (1이면 비공개, 0이면 공개) : " + isPrivate);
        System.out.println("file names : " + fileNames);

        //info 맵 bencode 수행
        byte[] bencodedInfoHash = Bencoder.encode(info);
        System.out.println("info Hash bencode 수행 완료 : " + bencodedInfoHash);

        //SHA-1 해시 계산
        MessageDigest sha1InfoHash = MessageDigest.getInstance("SHA-1");
        byte[] res = sha1InfoHash.digest(bencodedInfoHash);
        System.out.println("Info Hash 계산 완료 : " + res);
        System.out.println("=====================================");

        return res;
    }

    //torrent 파일 출력
    public void showTorrent(Map<String, Object> torrent) {
        System.out.println("\n\n======= [TORRENT MAP CONTENT] =======");

        for (Map.Entry<String, Object> entry : torrent.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // info 딕셔너리 내부 출력
                System.out.println("▶ " + key + " (Map):");
                Map<?, ?> subMap = (Map<?, ?>) value;
                for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
                    Object subValue = subEntry.getValue();
                    if (subValue instanceof byte[]) {
                        System.out.println("   - " + subEntry.getKey() + " (Binary): " + Arrays.toString((byte[]) subValue));
                    } else {
                        System.out.println("   - " + subEntry.getKey() + ": " + subValue);
                    }
                }
            } else if (value instanceof List) {
                // 파일 리스트 등 출력
                System.out.println("▶ " + key + " (List): " + value);
            } else if (value instanceof byte[]) {
                // info hash 등 바이너리 데이터 직접 출력
                System.out.println("▶ " + key + " (Binary): " + value);
            } else {
                // 일반 문자열이나 숫자 출력
                System.out.println("▶ " + key + ": " + value);
            }
        }

        System.out.println("=====================================\n\n");
    }
}