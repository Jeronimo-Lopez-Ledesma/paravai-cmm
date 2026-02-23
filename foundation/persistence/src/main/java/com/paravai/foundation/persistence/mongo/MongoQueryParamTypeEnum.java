package com.paravai.foundation.persistence.mongo;

/**
 * Tipos de datos manejados por el sistema.
 */
public enum MongoQueryParamTypeEnum {
    STRING {
        @Override
        public Object getConvertedValue(String s) {
            return s;
        }
    }
    , NUMBER{
        @Override
        public Object getConvertedValue(String s) {
            return s == null ? null : Long.valueOf(s);
        }
    };

    /**
     * Convierte una cadena en el tipo correcto según el tipo de parámetro.
     * @param s Cadena a convertir.
     * @return Cadena convertida. Puede ser null si el parámetro fue null.
     */
    public abstract Object getConvertedValue(String s);
}
