package gisscos.studentcard.entities.comparators;

import gisscos.studentcard.entities.PassRequest;

import java.util.Comparator;

/**
 * Компаратор для сравнения заявок по дате создания
 */
public class PassRequestCreationDateComparator implements Comparator<PassRequest> {

    @Override
    public int compare(PassRequest pr1, PassRequest pr2) {
        return pr1.getCreationDate().compareTo(pr2.getCreationDate());
    }
}
