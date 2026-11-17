/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.perkstore.notification.provider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.mail.MailPalette;
import org.exoplatform.portal.branding.MailDocumentShell;

import groovy.text.GStringTemplateEngine;

/**
 * The mails of the perk store, rendered as the mail channel renders them
 * (through the template engine), in the palette bound to their recipient: no
 * light colour literal is left, so a dark recipient gets dark colours.
 */
public class MailTemplatesPaletteTest {

  private static final Path         TEMPLATES       =
                                              Path.of("../perk-store-webapps/src/main/webapp/WEB-INF/conf/perk-store/templates/notification/mail");

  private static final List<String> NAMES           = List.of("OrderMailPlugin.gtmpl", "ProductMailPlugin.gtmpl");

  private static final String       DARK_SURFACE    = "#1E2328";

  private static final String       DARK_TEXT       = "#E6E6E6";

  private static final String       DARK_BORDER     = "#3A4148";

  private static final String       DARK_BACKGROUND = "#14181B";

  private static final String       DARK_LINK       = "#8AB4F8";

  @Test
  public void aDarkRecipientGetsTheDarkPalette() throws Exception {
    for (String template : NAMES) {
      Map<String, String> palette = new HashMap<>(MailDocumentShell.LIGHT_DEFAULTS);
      palette.put(MailDocumentShell.SURFACE, DARK_SURFACE);
      palette.put(MailDocumentShell.TEXT, DARK_TEXT);
      palette.put(MailDocumentShell.BORDER, DARK_BORDER);
      palette.put(MailDocumentShell.BACKGROUND, DARK_BACKGROUND);
      palette.put(MailDocumentShell.LINK, DARK_LINK);
      palette.put(MailPalette.MODE, "dark");

      String html = render(template, palette);

      assertTrue(template + " surface: " + html, html.contains("bgcolor=\"" + DARK_SURFACE + "\""));
      assertTrue(template + " text", html.contains("color:" + DARK_TEXT));
      assertTrue(template + " border", html.contains(DARK_BORDER));
      assertTrue(template + " background", html.contains(DARK_BACKGROUND));
      assertTrue(template + " link", html.contains("color:" + DARK_LINK));
      assertTrue(template + " role classes", html.contains("class=\"mail-surface mail-text\""));
      assertTrue(template + " content", html.contains("PRODUCT_TITLE_VALUE"));
      assertFalse(template + " keeps a light literal: " + html, hasLightLiteral(html));
    }
  }

  @Test
  public void aContextWithoutPaletteRendersInTheLightDefaults() throws Exception {
    for (String template : NAMES) {
      String html = render(template, null);

      assertTrue(template, html.contains("bgcolor=\"" + MailDocumentShell.LIGHT_DEFAULTS.get(MailDocumentShell.SURFACE) + "\""));
      assertFalse(template, html.contains("null"));
    }
  }

  private static boolean hasLightLiteral(String html) {
    String lower = html.toLowerCase();
    return List.of("#ffffff", "#333333", "#d8d8d8", "#f1f1f1", "#999999", "#c7c7c7", "#2f5e92", "#efefef", "#f9f9f9")
               .stream()
               .anyMatch(lower::contains);
  }

  private static String render(String template, Map<String, String> palette) throws Exception {
    StringWriter writer = new StringWriter();
    TemplateContext ctx = new TemplateContext();
    ctx.put("_writer", writer);
    ctx.put("_templateContext", ctx);
    ctx.put("_ctx", new TemplateHelper());
    for (String name : List.of("isNewOrder", "isNewProduct", "FIRSTNAME", "senderUrl", "senderName", "modifierName", "productTitle",
                               "productSupply", "productPrice", "symbol", "detailsURL", "orderId", "orderQuantity",
                               "orderStatus", "orderStatusLabel", "orderRefundedAmount", "orderRefundedQuantity",
                               "orderDeliveredQuantity", "orderRemainingQuantity")) {
      ctx.put(name, name.equals("productTitle") ? "PRODUCT_TITLE_VALUE" : "1");
    }
    ctx.put("isNewOrder", "true");
    ctx.put("isNewProduct", "true");
    ctx.put("detailsURL", "https://exo.example.com/portal/perkstore");
    if (palette != null) {
      ctx.put(MailPalette.CONTEXT_KEY, palette);
    }
    String source = Files.readString(TEMPLATES.resolve(template), StandardCharsets.UTF_8);
    new GStringTemplateEngine().createTemplate(source).make(ctx).writeTo(writer);
    return writer.toString();
  }

  /**
   * What a template calls on its {@code _ctx}: the labels are echoed back with
   * their arguments and the shared header and footer are not part of what is
   * rendered here.
   */
  public static class TemplateHelper {
    public String appRes(String key, Object... args) {
      return key + " " + java.util.Arrays.toString(args);
    }

    public String escapeHTML(String text) {
      return text;
    }

    public void include(String path, Object context) {
      // the shared header and footer have their own tests (commons)
    }
  }
}
