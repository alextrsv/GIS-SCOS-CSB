package ru.edu.online.entities.enums.converters;

import ru.edu.online.entities.enums.PassFileType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class PassFileTypeConverter implements AttributeConverter<PassFileType, String> {

    @Override
    public String convertToDatabaseColumn(PassFileType type) {
        if (type == null) {
            return null;
        }
        return type.getType();
    }

    @Override
    public PassFileType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(PassFileType.values())
                .filter(t -> t.getType().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}