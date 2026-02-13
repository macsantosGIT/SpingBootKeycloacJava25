package io.github.cursospring.sbootkeycloak;

import io.github.cursospring.sbootkeycloak.model.Curso;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("cursos")
public class CursoController {

    @GetMapping("java")
    public ResponseEntity<Curso> cursoJava(){
        return ResponseEntity.ok(new Curso(UUID.randomUUID(), "Java"));
    }

    @GetMapping("spring-boot")
    public ResponseEntity<Curso> cursoSpringBoot(){
        return ResponseEntity.ok(new Curso(UUID.randomUUID(), "Spring Boot"));
    }

    @GetMapping("exclusivos")
    public ResponseEntity<List<Curso>> cursoExclusivos(){
        Curso angular = new Curso(UUID.randomUUID(), "Ancular");
        Curso react = new Curso(UUID.randomUUID(), "React");
        Curso view = new Curso(UUID.randomUUID(), "View");

        return ResponseEntity.ok(List.of(angular,react,view));
    }

    @GetMapping("abertos")
    public ResponseEntity<List<Curso>> cursoAbertos(){
        Curso logica = new Curso(UUID.randomUUID(), "Lógica de Programação");
        Curso sql = new Curso(UUID.randomUUID(), "Sql");

        return ResponseEntity.ok(List.of(logica,sql));
    }

}
