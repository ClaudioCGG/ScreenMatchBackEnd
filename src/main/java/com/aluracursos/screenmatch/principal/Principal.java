package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private String API_KEY= "&apikey=da562f59";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    private SerieRepository repositorio;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Mostrar series por tìtulo
                    5 - Top 5 series
                    6 - Buscar series por categoría
                    7 - Filtrar series
                    8 - Crear Categoria
                    9 - Top 5 episodios por Serie
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    mostrarSeriesPorTitulo();
                    break;

                case 5:

                    buscarTop5Series();
                    break;
                case 6:buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarTemporadasCalificacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }



    private void buscarEpisodiosPorTitulo() {
        System.out.println("Escribe el nombre del episodio que deseas buscar");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s Temporada %s Episodio %s Evaluación %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));


    }



    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        var nombreGenero = teclado.nextLine();
        Categoria categoria = Categoria.fromEspanol(nombreGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries da categoria " + nombreGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Serie: " + s.getTitulo() + "Evaluación: " + s.getEvaluacion()));
    }

    private void filtrarTemporadasCalificacion() {
        System.out.println("Indicar cantidad de temporadas minimas:");
        var cantTemporadasMin = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Indicar evalucion minimas:");
        var evalMin = teclado.nextDouble();
        teclado.nextLine();

        List<Serie> serieFiltroTempEval = repositorio.seriesPorTemparadaYEvaluacion(cantTemporadasMin, evalMin);
        System.out.println("*** Series filtradas ***");

        serieFiltroTempEval.forEach(s -> System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));

    }


    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {

        // DatosSeries datosSeries la voy a comentar ya que no noy a realizar un requerimiento a la API, ahora lo s que
        // quiero es que busque las series buscadas y guardadas en mi BBDD y utilizar el metodo mostrarSeriesBuscadas()
        // generadas en el Principal que r realiza esto.

        // DatosSerie datosSerie = getDatosSerie();

        mostrarSeriesBuscadas();

        System.out.println("Escribe el nombre de las series buscadas que quieras saber los episodios");
        var nombreSerie = teclado.nextLine();

        // Agregar un Opcional que va a realizar una busqueda que puede encontrar o no resultado
        // Tener presente aquì que estoy creando los datos de la serie en base a las series buscadas y stream esta lista
        Optional <Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            //Acá busco la cantidad de temporadas de la serie buscada,
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

        }


    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        // A List<Series> series voy a modificarla operacionalmente para que este disponible a nivel global y no solo en esta funcion
        // List<Serie> series = repositorio.findAll();

        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }


    private void mostrarSeriesPorTitulo() {
        System.out.println("Escribe el titulo de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        Optional<Serie> serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if(serieBuscada.isPresent()) {
            System.out.println(" La serie buscada es: " + '\n' );
            System.out.println(" Titulo: " + serieBuscada.get().getTitulo().toUpperCase());
            System.out.println(" Temporadas: " + serieBuscada.get().getTotalTemporadas());
            System.out.println(" Evaluacion: " + serieBuscada.get().getEvaluacion());
            System.out.println(" Sinopsis: " + serieBuscada.get().getSinopsis() + '\n');
        } else  {
            System.out.println("Serie no encontrada");
        };
    }


}