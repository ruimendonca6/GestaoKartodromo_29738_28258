package pt.kartodromo.web;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.config.HibernateUtil;

public class WebApplication {

    public static void main(String[] args) {
        CategoriaKartService categoriaService = new CategoriaKartService();
        int totalCategorias = categoriaService.listarCategorias().size();

        System.out.println("Modulo web iniciado.");
        System.out.println("Categorias disponiveis: " + totalCategorias);

        HibernateUtil.shutdown();
    }
}
