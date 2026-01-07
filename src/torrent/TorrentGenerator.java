package torrent;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
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
            String rootFolder, String trackerUrl, List<String> announceList, List<String> filePaths,
            String createdBy, String comment, int pieceLength, boolean isPrivate, String encoding
    ) throws Exception {
        System.out.println("========== Torrent file Generate Start ==========");

        // 1. pieces hash 계산
        byte[] pieces = calPieces(rootFolder, filePaths, pieceLength);

        // 2. info 구성 (TreeMap 사용 - 사전순 정렬 필수)
        Map<String, Object> info = new TreeMap<>();
        info.put("files", generateFilesList(rootFolder, filePaths));
        info.put("root-folder", rootFolder); // 폴더명
        info.put("piece length", (long) pieceLength); // 표준: piece length
        info.put("pieces", pieces);
        info.put("private", isPrivate ? 1L : 0L);

        // 3. 전체 토렌트 구성 (TreeMap 사용)
        Map<String, Object> torrent = new TreeMap<>();
        torrent.put("announce", trackerUrl);
        if (announceList != null && !announceList.isEmpty()) {
            torrent.put("announce-list", announceList);
        }
        torrent.put("comment", comment);
        torrent.put("created by", createdBy);
        torrent.put("creation date", System.currentTimeMillis() / 1000);
        torrent.put("encoding", encoding);
        torrent.put("info", info);

        System.out.println("====================================================");
        return torrent;
    }

    // path
    // "music/pop/song.mp3" -> ["music", "pop", "song.mp3"]
    private List<Map<String, Object>> generateFilesList(String rootFolder, List<String> filePaths) {
        List<Map<String, Object>> files = new ArrayList<>();

        for(String filePath : filePaths){
            File file = new File(rootFolder, filePath);
            if(!file.exists()) continue;

            Map<String, Object> fileInfo = new TreeMap<>();
            fileInfo.put("length", file.length());

            //경로 분리 로직
            List<String> pathList = Arrays.stream(filePath.split("[/\\\\]"))
                    .filter(s->!s.isEmpty())
                    .toList();
            fileInfo.put("path", pathList);
            files.add(fileInfo);
        }
        return files;
    }

    // pieces hash 계산 (기존 로직 유지)
    public byte[] calPieces(String fileDirectory, List<String> filePaths, int pieceLength) throws Exception {
        System.out.println("===============Cal pieces Hash=============");
        ByteArrayOutputStream allHashes = new ByteArrayOutputStream();
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[pieceLength];
        int bufferOffset = 0;

        for (String fileName : filePaths) {
            File file = new File(fileDirectory, fileName);
            if (!file.exists()) continue;
            try (FileInputStream fis = new FileInputStream(file)) {
                int read;
                while ((read = fis.read(buffer, bufferOffset, pieceLength - bufferOffset)) != -1) {
                    bufferOffset += read;
                    if (bufferOffset == pieceLength) {
                        allHashes.write(sha1.digest(buffer));
                        sha1.reset();
                        bufferOffset = 0;
                    }
                }
            }
        }
        if (bufferOffset > 0) {
            sha1.update(buffer, 0, bufferOffset);
            allHashes.write(sha1.digest());
        }
        return allHashes.toByteArray();
    }

    // info hash 계산
    public byte[] calInfoHash(byte[] btorrent) throws Exception {
        System.out.println("========== Info Hash Calculation Start ==========");

        // 디코딩 단계 (수정된 Bdecoder가 TreeMap을 반환하므로 순서가 유지됨)
        Map<String, Object> torrent = Bdecoder.decode(btorrent);
        Object info = torrent.get("info");

        // 다시 인코딩 (TreeMap 덕분에 키가 정렬되어 인코딩됨)
        byte[] bencodeInfo = Bencoder.encode(info);

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] infoHash = sha1.digest(bencodeInfo);

        System.out.println("Info Hash (Hex): " + infoHash);
        return infoHash;
    }



    public byte[] bencodeTorrent(Map<String, Object> torrent) throws Exception {
        return Bencoder.encode(torrent);
    }
    public Map<String, Object> bdecodeTorrent(byte[] encodedTorrent) throws Exception{
        return Bdecoder.decode(encodedTorrent);
    }


    //--------------------------------------------------------------------------------------------------
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
                    System.out.println("[byte array, length: " + ((byte[]) v).length + "]");
                } else if (v instanceof Map || v instanceof List) {
                    System.out.println(); // 복합 객체는 다음 줄부터
                    printRecursive(v, indent + 1);
                } else {
                    System.out.println(v); // 일반 값 출력
                }
            });
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            // 리스트의 요소가 문자열이면 한 줄에 출력 (path 처리용)
            if (!list.isEmpty() && list.get(0) instanceof String) {
                System.out.println(space + "  " + list.toString());
            } else {
                for (int i = 0; i < list.size(); i++) {
                    System.out.println(space + "  [" + i + "]");
                    printRecursive(list.get(i), indent + 2);
                }
            }
        }
    }

}