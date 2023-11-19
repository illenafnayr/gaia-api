package com.gaia.api.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class BatchService {

    public String runPythonScript() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "path/to/myscript.py");
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        process.waitFor();

        // Read the output of the Python script
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        return output.toString();
    }
}
