package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Lesson.LessonView;
import com.BUSY.learnWithUs.Dto.Lesson.ReorderLessonsRequest;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.FileStorageService;
import com.BUSY.learnWithUs.Service.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class LessonController {

    private final AuthService authService;
    private final LessonService lessonService;
    private final FileStorageService fileStorageService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'LEARNER')")
    @GetMapping("/courses/{id}/lessons")
    public List<LessonView> lessons(@PathVariable Long id) {
        return lessonService.getLessons(me(), id);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(
            value = "/courses/{id}/lessons",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public LessonView addLesson(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) Integer position,
            @RequestPart("file") MultipartFile file
    ) {
        return lessonService.addLesson(
                me(),
                id,
                title,
                position,
                file
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping(
            value = "/lessons/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public LessonView updateLesson(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) Integer position,
            @RequestPart(value = "file", required = false)
            MultipartFile file
    ) {
        return lessonService.updateLesson(
                me(),
                id,
                title,
                position,
                file
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'LEARNER')")
    @GetMapping("/lessons/{id}/file")
    public ResponseEntity<Resource> lessonFile(
            @PathVariable Long id
    ) throws Exception {

        Path path = lessonService.getLessonFilePath(me(), id);
        Resource resource = new UrlResource(path.toUri());

        String contentType = Files.probeContentType(path);

        if (contentType == null) {
            contentType = mediaTypeFor(path);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(path.getFileName().toString())
                                .build()
                                .toString()
                )
                .body(resource);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long id
    ) {
        lessonService.deleteLesson(me(), id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/courses/{id}/lessons/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable Long id,
            @RequestBody ReorderLessonsRequest request
    ) {
        lessonService.reorder(me(), id, request.lessonIds());
        return ResponseEntity.noContent().build();
    }

    private String mediaTypeFor(Path path) {
        String name = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (name.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }

        if (name.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }

        if (name.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
