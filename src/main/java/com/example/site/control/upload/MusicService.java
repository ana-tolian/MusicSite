package com.example.site.control.upload;

import com.example.site.entity.Playlist;
import com.example.site.entity.Soundtrack;
import com.example.site.repository.JdbcSoundtrackRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class MusicService {

    public static String uploadDirectory = "/uploads";
    private final JdbcSoundtrackRepository soundtrackRepository;


    public MusicService (JdbcSoundtrackRepository soundtrackRepository) {
        this.soundtrackRepository = soundtrackRepository;
    }

    public void store(MultipartFile [] files, List<String> list) {
        for (MultipartFile file : files) {
            Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
            list.add(fileNameAndPath.getFileName().toString());

            try {
                if (!Files.exists(fileNameAndPath.getParent()))
                    Files.createDirectories(fileNameAndPath.getParent());

                Files.write(fileNameAndPath, file.getBytes());

                String soundtrackName = prepareToSave(fileNameAndPath.getFileName().toString());


                soundtrackRepository.save(new Soundtrack(
                        getName(soundtrackName),
                        getArtist(soundtrackName),
                        uploadDirectory + "/" + fileNameAndPath.getFileName().toString(),
                        null, null));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Soundtrack> loadAllSoundtracks() {
        return soundtrackRepository.findAll();
    }

    private String prepareToSave (String filename) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename.replace('_', ' ');
        filename = filename.replace('-', '???');
        filename = filename.replace('???', '???');

        int count = countDashes(filename);
        if (count == 1)
            return filename;

        else if (count >= 2) {
            int index = filename.substring(filename.indexOf('???') + 1).indexOf('???');
            filename = filename.substring(0, index) + filename.substring(index + 1); // substr(0, index of second '???') + substr(index of second '???');
        }

        return filename;
    }

    private int countDashes (String str) {
        char [] ch = str.toCharArray();
        int count = 0;

        for (int i = 0; i < ch.length; i++)
            if (ch[i] == '???')
                count++;

        return count;
    }

    private String getName (String fileName) {
        return fileName.substring(fileName.lastIndexOf('???') + 1);
    }

    private String getArtist (String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('???'));
    }

    public List<Playlist> getAllPlaylists () {
        return soundtrackRepository.findAllPlaylists();
    }

    public Path load(String filename) {
        return null;
    }

    public long getFileLength (String filename) {
        File file = new File("/uploads/" + filename);
        return file.length();
    }
    public Resource loadAsResource(String filename) {
        File file = new File("/uploads/" + filename);

        InputStreamResource resource = null;

        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return resource;
    }

    public void deleteAll() {

    }
}
