package com.aluracursos.screenmatch;

import com.aluracursos.screenmatch.principal.Principal;
import com.aluracursos.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

//Voy a borrar la implementación de CommandLineRunner ya que será web
//public class ScreenmatchApplication implements CommandLineRunner {

public class ScreenmatchApplication {

// Voy a eliminar esta implementación que nos servía para trabajar operaciones CRUD en nuestra base de datos
//	@Autowired
//	private SerieRepository repository;
	public static void main(String[] args) {
		//este método es correcto ya que este método me permite ejecutar mi aplicación
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

// También voy a elimiar esta interfas que ya no la estoy utilizando al eliminar la inyecciòn del repository
//	@Override
//	public void run(String... args) throws Exception {
//		Principal principal = new Principal(repository);
//		principal.muestraElMenu();




}
