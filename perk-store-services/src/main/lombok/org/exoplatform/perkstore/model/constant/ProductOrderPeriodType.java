/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.perkstore.model.constant;

import static org.exoplatform.perkstore.service.utils.Utils.timeToMilliseconds;

import java.time.*;

import org.exoplatform.perkstore.model.ProductOrderPeriod;

public enum ProductOrderPeriodType {
  WEEK("Week", "week"),
  MONTH("Month", "month"),
  QUARTER("Quarter", "quarter"),
  SEMESTER("Semester", "semester"),
  YEAR("Year", "year"),
  NONE("None", "none");

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
    ProductOrderPeriod productPeriod = new ProductOrderPeriod();
    YearMonth yearMonth = YearMonth.from(localDateTime);
    switch (this) {
    case WEEK:
      LocalDateTime firstDayOfThisWeek = localDateTime.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
      LocalDateTime firstDayOfNextWeek = firstDayOfThisWeek.plusWeeks(1);
      productPeriod.setStartDate(timeToMilliseconds(firstDayOfThisWeek));
      productPeriod.setEndDate(timeToMilliseconds(firstDayOfNextWeek));
      break;
    case MONTH:
      YearMonth currentMonth = yearMonth;
      YearMonth nextMonth = currentMonth.plusMonths(1);
      productPeriod.setStartDate(timeToMilliseconds(currentMonth.atDay(1).atStartOfDay()));
      productPeriod.setEndDate(timeToMilliseconds(nextMonth.atDay(1).atStartOfDay()));
      break;
    case QUARTER:
      int monthQuarterIndex = ((yearMonth.getMonthValue() - 1) / 3) * 3 + 1;

      YearMonth startQuarterMonth = YearMonth.of(yearMonth.getYear(), monthQuarterIndex);
      YearMonth endQuarterMonth = startQuarterMonth.plusMonths(3);
      productPeriod.setStartDate(timeToMilliseconds(startQuarterMonth.atDay(1).atStartOfDay()));
      productPeriod.setEndDate(timeToMilliseconds(endQuarterMonth.atDay(1).atStartOfDay()));
      break;
    case SEMESTER:
      int monthSemesterIndex = ((yearMonth.getMonthValue() - 1) / 6) * 6 + 1;

      YearMonth startSemesterMonth = YearMonth.of(yearMonth.getYear(), monthSemesterIndex);
      YearMonth endSemesterMonth = startSemesterMonth.plusMonths(6);
      productPeriod.setStartDate(timeToMilliseconds(startSemesterMonth.atDay(1).atStartOfDay()));
      productPeriod.setEndDate(timeToMilliseconds(endSemesterMonth.atDay(1).atStartOfDay()));
      break;
    case YEAR:
      productPeriod.setStartDate(timeToMilliseconds(Year.from(localDateTime).atDay(1).atStartOfDay()));
      productPeriod.setEndDate(timeToMilliseconds(Year.from(localDateTime).plusYears(1).atDay(1).atStartOfDay()));
      break;
    default:
    }
    return productPeriod;
  }
}
