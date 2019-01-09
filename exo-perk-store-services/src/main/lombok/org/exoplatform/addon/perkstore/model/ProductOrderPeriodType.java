package org.exoplatform.addon.perkstore.model;

import static org.exoplatform.addon.perkstore.service.utils.Utils.timeToMilliseconds;

import java.time.*;

public enum ProductOrderPeriodType {
  WEEK("Week", "week"), MONTH("Month", "month"), QUARTER("Quarter", "quarter"), SEMESTER("Semester", "semester"), YEAR("Year",
      "year");

  private String name;

  private String label;

  private ProductOrderPeriodType(String name, String label) {
    this.name = name;
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public ProductOrderPeriod getPeriodOfTime(LocalDateTime localDateTime) {
    ProductOrderPeriod kudosPeriod = new ProductOrderPeriod();
    YearMonth yearMonth = YearMonth.from(localDateTime);
    switch (this) {
    case WEEK:
      LocalDateTime firstDayOfThisWeek = localDateTime.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
      LocalDateTime firstDayOfNextWeek = firstDayOfThisWeek.plusWeeks(1);
      kudosPeriod.setStartDate(timeToMilliseconds(firstDayOfThisWeek));
      kudosPeriod.setEndDate(timeToMilliseconds(firstDayOfNextWeek));
      break;
    case MONTH:
      YearMonth currentMonth = yearMonth;
      YearMonth nextMonth = currentMonth.plusMonths(1);
      kudosPeriod.setStartDate(timeToMilliseconds(currentMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDate(timeToMilliseconds(nextMonth.atDay(1).atStartOfDay()));
      break;
    case QUARTER:
      int monthQuarterIndex = ((yearMonth.getMonthValue() - 1) / 3) * 3 + 1;

      YearMonth startQuarterMonth = YearMonth.of(yearMonth.getYear(), monthQuarterIndex);
      YearMonth endQuarterMonth = startQuarterMonth.plusMonths(3);
      kudosPeriod.setStartDate(timeToMilliseconds(startQuarterMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDate(timeToMilliseconds(endQuarterMonth.atDay(1).atStartOfDay()));
      break;
    case SEMESTER:
      int monthSemesterIndex = ((yearMonth.getMonthValue() - 1) / 6) * 6 + 1;

      YearMonth startSemesterMonth = YearMonth.of(yearMonth.getYear(), monthSemesterIndex);
      YearMonth endSemesterMonth = startSemesterMonth.plusMonths(6);
      kudosPeriod.setStartDate(timeToMilliseconds(startSemesterMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDate(timeToMilliseconds(endSemesterMonth.atDay(1).atStartOfDay()));
      break;
    case YEAR:
      kudosPeriod.setStartDate(timeToMilliseconds(Year.from(localDateTime).atDay(1).atStartOfDay()));
      kudosPeriod.setEndDate(timeToMilliseconds(Year.from(localDateTime).plusYears(1).atDay(1).atStartOfDay()));
      break;
    }
    return kudosPeriod;
  }
}
