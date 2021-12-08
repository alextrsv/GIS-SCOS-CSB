package ru.edu.online.entities.enums.converters;

import ru.edu.online.entities.enums.PRFileType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class PassFileTypeConverter implements AttributeConverter<PRFileType, String> {

    @Override
    public String convertToDatabaseColumn(PRFileType type) {
        if (type == null) {
            return null;
        }
        return type.getType();
    }

    @Override
    public PRFileType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(PRFileType.values())
                .filter(t -> t.getType().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}