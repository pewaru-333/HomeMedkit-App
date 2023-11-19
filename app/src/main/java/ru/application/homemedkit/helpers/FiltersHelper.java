package ru.application.homemedkit.helpers;

import java.util.Comparator;

import ru.application.homemedkit.databaseController.Medicine;

public class FiltersHelper {
    public static final Comparator<Medicine> sortInverseTitle = Comparator.comparing(o -> o.productName);

    public static final Comparator<Medicine> sortReverseTitle = (o1, o2) -> o2.productName.compareTo(o1.productName);

    public static final Comparator<Medicine> sortInverseDate = Comparator.comparing(o -> o.expDate);

    public static final Comparator<Medicine> sortReverseDate = (o1, o2) -> Long.compare(o2.expDate, o1.expDate);
}
