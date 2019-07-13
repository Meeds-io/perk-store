package org.exoplatform.addon.perkstore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.Test;

import org.exoplatform.addon.perkstore.model.ProductOrderPeriod;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderPeriodType;

public class ProductOrderPeriodTypeTest {

  @Test
  public void testGetWeekPeriod() {
    ProductOrderPeriod periodOfTime = ProductOrderPeriodType.WEEK.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTime = LocalDate.of(2019, 7, 8).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
    long endTime = LocalDate.of(2019, 7, 15).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

    ProductOrderPeriod expectedPeriod = new ProductOrderPeriod(startTime, endTime);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTime, periodOfTime.getStartDate());
    assertEquals(endTime, periodOfTime.getEndDate());
  }

  @Test
  public void testGetMonthPeriod() {
    ProductOrderPeriod periodOfTime = ProductOrderPeriodType.MONTH.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTime = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
    long endTime = LocalDate.of(2019, 8, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

    ProductOrderPeriod expectedPeriod = new ProductOrderPeriod(startTime, endTime);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTime, periodOfTime.getStartDate());
    assertEquals(endTime, periodOfTime.getEndDate());
  }

  @Test
  public void testGetQuarterPeriod() {
    ProductOrderPeriod periodOfTime = ProductOrderPeriodType.QUARTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTime = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
    long endTime = LocalDate.of(2019, 10, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

    ProductOrderPeriod expectedPeriod = new ProductOrderPeriod(startTime, endTime);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTime, periodOfTime.getStartDate());
    assertEquals(endTime, periodOfTime.getEndDate());
  }

  @Test
  public void testGetSemesterPeriod() {
    ProductOrderPeriod periodOfTime = ProductOrderPeriodType.SEMESTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTime = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
    long endTime = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

    ProductOrderPeriod expectedPeriod = new ProductOrderPeriod(startTime, endTime);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTime, periodOfTime.getStartDate());
    assertEquals(endTime, periodOfTime.getEndDate());
  }

  @Test
  public void testGetYearPeriod() {
    ProductOrderPeriod periodOfTime = ProductOrderPeriodType.YEAR.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTime = LocalDate.of(2019, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
    long endTime = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

    ProductOrderPeriod expectedPeriod = new ProductOrderPeriod(startTime, endTime);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(expectedPeriod.hashCode(), periodOfTime.hashCode());
    assertEquals(startTime, periodOfTime.getStartDate());
    assertEquals(endTime, periodOfTime.getEndDate());
  }
}
