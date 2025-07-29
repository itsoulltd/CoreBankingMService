package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.it.soul.lab.connect.io.ScriptRunner;
import com.itsoul.lab.ledgerbook.connector.SourceConnector;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class InitializeVAccountDB extends LedgerTask {

    private SourceConnector connector;
    protected SourceConnector getConnector(){return connector;}

    public InitializeVAccountDB(SourceConnector connector) {
        super(null);
        this.connector = connector;
    }

    @Override
    public Message execute(Message message) throws RuntimeException {Response response = new Response();
        SourceConnector connector = getConnector();
        ScriptRunner runner = new ScriptRunner();
        //Fixing using Spring ClassPathResource.java
        //File file = new ClassPathResource(connector.schema()).getFile();
        //
        try(InputStream stream = new ClassPathResource(connector.schema()).getInputStream();
            Connection connection = connector.getConnection()) {
            //
            String[] cmds = runner.commands(stream);
            runner.execute(cmds, connection);
        } catch (SQLException | FileNotFoundException e) {
            response.setError(e.getMessage());
            return response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setMessage("VAccount DB Schema creation successful!");
        return response.setStatus(HttpStatus.CREATED.value());
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return null;
    }
}
