package br.com.munif.framework.vicente.core;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    public static String removeNaoNumeros(String numero) {
        if (numero == null) {
            return null;
        }
        return numero.replaceAll("[\\D]", "");
    }

    public static void removeNumerosDosAtributos(Object obj, String... atributos) {
        Class clazz = obj.getClass();
        for (String atributo : atributos) {
            try {
                Field att = clazz.getDeclaredField(atributo);
                att.setAccessible(true);
                att.set(obj, removeNaoNumeros((String) att.get(obj)));
            } catch (NoSuchFieldException ex) {
                System.out.println("--->Atributo não existe");
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String args[]) {

    }

}
