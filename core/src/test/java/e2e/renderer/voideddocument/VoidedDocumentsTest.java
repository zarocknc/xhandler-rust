/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License - 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package e2e.renderer.voideddocument;

import e2e.AbstractTest;
import e2e.renderer.XMLAssertUtils;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog1_Invoice;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.sunat.baja.VoidedDocuments;
import io.github.project.openubl.xbuilder.content.models.sunat.baja.VoidedDocumentsItem;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.github.project.openubl.xbuilder.renderer.TemplateProducer;
import io.quarkus.qute.Template;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static e2e.renderer.XMLAssertUtils.assertSendSunat;
import static e2e.renderer.XMLAssertUtils.assertSnapshot;

public class VoidedDocumentsTest extends AbstractTest {

    @Test
    public void testMultipleVoidedDocuments() throws Exception {
        // Given
        VoidedDocuments input = VoidedDocuments.builder()
                .numero(1)
                .fechaEmision(LocalDate.of(2022, 01, 31))
                .fechaEmisionComprobantes(LocalDate.of(2022, 01, 29))
                .proveedor(Proveedor.builder()
                        .ruc("12345678912")
                        .razonSocial("Softgreen S.A.C.")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(1)
                        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
                        .descripcionSustento("Mi sustento1")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(2)
                        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
                        .descripcionSustento("Mi sustento2")
                        .build()
                )
                .build();

        ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
        enricher.enrich(input);

        // When
        Template template = TemplateProducer.getInstance().getVoidedDocument();
        String xml = template.data(input).render();

        // Then
        assertSnapshot(xml, getClass(), "voidedDocument.xml");
        assertSendSunat(xml, XMLAssertUtils.VOIDED_DOCUMENTS_XSD);
    }

    @Test
    public void testMultipleVoidedDocuments_autoGeneratedFechaEmision() throws Exception {
        // Given
        VoidedDocuments input = VoidedDocuments.builder()
                .numero(1)
                .fechaEmisionComprobantes(dateProvider.now().minusDays(2))
                .proveedor(Proveedor.builder()
                        .ruc("12345678912")
                        .razonSocial("Softgreen S.A.C.")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(1)
                        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
                        .descripcionSustento("Mi sustento1")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(2)
                        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
                        .descripcionSustento("Mi sustento2")
                        .build()
                )
                .build();

        ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
        enricher.enrich(input);

        // When
        Template template = TemplateProducer.getInstance().getVoidedDocument();
        String xml = template.data(input).render();

        // Then
        assertSnapshot(xml, getClass(), "voidedDocument_autoGeneratedFechaEmision.xml");
        assertSendSunat(xml, XMLAssertUtils.VOIDED_DOCUMENTS_XSD);
    }

    @Test
    public void testMultipleVoidedDocuments_autoGeneratedTipoComprobante() throws Exception {
        // Given
        VoidedDocuments input = VoidedDocuments.builder()
                .numero(1)
                .fechaEmision(LocalDate.of(2022, 01, 31))
                .fechaEmisionComprobantes(LocalDate.of(2022, 01, 29))
                .proveedor(Proveedor.builder()
                        .ruc("12345678912")
                        .razonSocial("Softgreen S.A.C.")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(1)
                        .descripcionSustento("Mi sustento1")
                        .build()
                )
                .comprobante(VoidedDocumentsItem.builder()
                        .serie("F001")
                        .numero(2)
                        .descripcionSustento("Mi sustento2")
                        .build()
                )
                .build();

        ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
        enricher.enrich(input);

        // When
        Template template = TemplateProducer.getInstance().getVoidedDocument();
        String xml = template.data(input).render();

        // Then
        assertSnapshot(xml, getClass(), "voidedDocument_autoGeneratedTipoComprobante.xml");
        assertSendSunat(xml, XMLAssertUtils.VOIDED_DOCUMENTS_XSD);
    }
}
