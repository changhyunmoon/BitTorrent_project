package torrent;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.*;

public class TorrentGenerator {


    //torrent 파일 생성
//    encoding(String) :
//    announce(String) :
//    announce-list(List<>) :
//    created-by(String) :
//    created-date(String) :
//    comment(String) :
//    info(List<>)
//        root-folder(String) :
//        files(Map<>) :
//            length(int) :
//            path(String) :
//        piece-length(int) :
//        pieces(byte[]) :
//        isprivate(int) :

    public Map<String, Object> generateMultiFileTorrent(
            String rootFolder, String trackerUrl, List<String> announceList, List<String> fileNames,
            String createdBy, String comment, int pieceLength, boolean isPrivate, String encoding
    ) throws Exception {
        System.out.println("==========Torrent file Generate Start==========");
        //1. pieces hash 계산
        byte[] pieces = calPieces(rootFolder, fileNames, pieceLength);

        //2. info 구성
        Map<String, Object> info = new HashMap<>();
        info.put("root-folder", rootFolder);
        info.put("piece-length", pieceLength);
        info.put("pieces", pieces);
        info.put("private", isPrivate ? 1 : 0);

        //다중 파일 files 리스트 생성
        List<Map<String, Object>> files = new ArrayList<>();
        for(String fileName : fileNames){
            Map<String, Object> fileInfo = new HashMap<>();
            File file = new File(rootFolder, fileName);
            fileInfo.put("length", file.length());
            fileInfo.put("path", fileName);
            System.out.println("length : " + file.length() + ", path : " + fileName);
            files.add(fileInfo);
        }
        info.put("files", files);

        //torrent 딕셔너리 구성
        Map<String, Object> torrent = new HashMap<>();
        torrent.put("announce", trackerUrl);
        if (announceList != null && !announceList.isEmpty()) {
            torrent.put("announce-list", announceList);
        }else{
            torrent.put("announce-list", null);
        }
        torrent.put("created by", createdBy);
        torrent.put("creation date", System.currentTimeMillis() / 1000);
        torrent.put("comment", comment);
        torrent.put("encoding", encoding);
        torrent.put("info", info); // info 딕셔너리를 통째로 포함

        System.out.println("====================================================");
    return torrent;

    }

    //pieces hash 계산
    public byte[] calPieces(String fileDirectory, List<String> fileNames, int pieceLength) throws Exception {
        ByteArrayOutputStream allHashes = new ByteArrayOutputStream();
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        byte[] buffer = new byte[pieceLength];
        int bufferOffset = 0;

        // 파일을 순서대로 읽어서 하나의 거대한 파일처럼 처리
        for (String fileName : fileNames) {
            File file = new File(fileDirectory, fileName);
            if (!file.exists()) continue;

            try (FileInputStream fis = new FileInputStream(file)) {
                int read;
                while ((read = fis.read(buffer, bufferOffset, pieceLength - bufferOffset)) != -1) {
                    bufferOffset += read;

                    // 버퍼가 pieceLength만큼 차면 즉시 해시 계산
                    if (bufferOffset == pieceLength) {
                        allHashes.write(sha1.digest(buffer));
                        sha1.reset();
                        bufferOffset = 0;
                    }
                }
            }
        }

        // 마지막 남은 자투리 데이터 처리
        if (bufferOffset > 0) {
            sha1.update(buffer, 0, bufferOffset);
            allHashes.write(sha1.digest());
        }

        return allHashes.toByteArray();
    }

    //torrent 파일 bencoding
    public byte[] bencodeTorrent(Map<String, Object> torrent) throws Exception{

        byte[] res = Bencoder.encode(torrent);

        return res;
    }
    //torrent 파일 bdecoding
    public Map<String, Object> bdecodeTorrent(byte[] btorrent) throws Exception{
        Map<String, Object> torrent = Bdecoder.decode(btorrent);
        return torrent;
    }

    //info hash 계산
    //bencode 된 torrent 파일에서 계산
    public byte[] calInfoHash(byte[] btorrent) throws Exception{
        System.out.println("==========Hash info calculate Start==========");

        //bencode된 torrent -> decode 된 torrent
        Map<String, Object> torrent = Bdecoder.decode(btorrent);

        //info 추출
        Object info = torrent.get("info");
        if(info == null) throw new Exception("No info section found from torrent file");

        //info 부분만 다시 bencode
        byte[] bencodeInfo = Bencoder.encode(info);
        //해싱
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] infoHash = sha1.digest(bencodeInfo);

        System.out.println("info 정보 출력 : " + info);
        System.out.println("info hash 값 : " + infoHash);
        System.out.println("=================================================");
        return infoHash;
    }


    //torrent 파일 출력
    public void printTorrent(Map<String, Object> torrent){
        System.out.println("==========Show Torrent==========");
        printRecursive(torrent, 0);
        System.out.println("=================================");
    }
    private void printRecursive(Object obj, int indent) {
        String space = "  ".repeat(indent);

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            map.forEach((k, v) -> {
                System.out.print(space + "▶ " + k + " : ");
                if (v instanceof byte[]) {
                    // 바이트 배열을 16진수 변환 없이 [byte, byte, ...] 형태로 출력
                    System.out.println(Arrays.toString((byte[]) v));
                } else if (v instanceof Map || v instanceof List) {
                    System.out.println();
                    printRecursive(v, indent + 1);
                } else {
                    System.out.println(v);
                }
            });
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                System.out.println(space + "  [" + i + "]");
                printRecursive(list.get(i), indent + 2);
            }
        }
    }

}