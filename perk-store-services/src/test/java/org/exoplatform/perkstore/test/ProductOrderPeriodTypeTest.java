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
package org.exoplatform.perkstore.test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.Test;

import org.exoplatform.perkstore.model.ProductOrderPeriod;
import org.exoplatform.perkstore.model.constant.ProductOrderPeriodType;

public class ProductOrderPeriodTypeTest extends BasePerkStoreTest {

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
