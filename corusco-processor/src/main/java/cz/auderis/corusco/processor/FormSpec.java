package cz.auderis.corusco.processor;

import java.util.List;

/**
 * Normalized description of one generated form source.
 *
 * <p>The processor builds this model from either record components or abstract
 * accessor methods before writing generated sources. The source type remains
 * the public owner/result type for generated keys, descriptors, and form
 * models. Abstract class sources additionally have a generated immutable result
 * implementation.</p>
 */
final class FormSpec {

    final String formId;
    final String sourceType;
    final String resultImplementationType;
    final List<FieldSpec> fields;

    FormSpec(String formId, String sourceType, String resultImplementationType, List<FieldSpec> fields) {
        this.formId = formId;
        this.sourceType = sourceType;
        this.resultImplementationType = resultImplementationType;
        this.fields = List.copyOf(fields);
    }

    boolean hasGeneratedResultImplementation() {
        return resultImplementationType != null;
    }
}
