package com.koyta.auth.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koyta.auth.exceptions.ContractValidationException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
public class JsonContractValidator {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    public void validate(String requestBody, String schemaPath) {

        try {
            JsonNode jsonNode = mapper.readTree(requestBody);

            JsonSchema schema = schemaFactory.getSchema(
                    new ClassPathResource(schemaPath).getInputStream()
            );

            Set<ValidationMessage> errors = schema.validate(jsonNode);

            if (!errors.isEmpty()) {
                String message = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .findFirst()
                        .orElse("Invalid request payload");

                throw new ContractValidationException(message);
            }

        } catch (JsonProcessingException e) {
            throw new ContractValidationException("Malformed JSON request");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON schema");
        }
    }
}
