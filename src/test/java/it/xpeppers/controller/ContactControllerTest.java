package it.xpeppers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.xpeppers.model.Contact;
import it.xpeppers.repository.ContactRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static it.xpeppers.model.ContactTest.aContact;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class ContactControllerTest {

    private static final Integer ID = 1;
    private static final String FIRST_NAME = "A FIRST NAME";
    private static final String LAST_NAME = "A LAST NAME";
    private static final String TELEPHONE_NUMBER = "+39 02 123456";
    private static final String ANOTHER_FIRST_NAME = "ANOTHER FIRST NAME";

    @InjectMocks
    private ContactController controller;

    @Mock
    private ContactRepository repository;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    public void returns_all_contacts() throws Exception {
        Contact contact = aContact(FIRST_NAME, LAST_NAME, TELEPHONE_NUMBER);
        contact.setId(ID);

        when(repository.findAll()).thenReturn(singletonList(contact));

        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(ID)))
                .andExpect(jsonPath("$[0].firstName", is(FIRST_NAME)))
                .andExpect(jsonPath("$[0].lastName", is(LAST_NAME)))
                .andExpect(jsonPath("$[0].phoneNumber", is(TELEPHONE_NUMBER)));
    }

    @Test
    public void returns_a_contact_with_a_given_id() throws Exception {
        Contact contact = aContact(FIRST_NAME, LAST_NAME, TELEPHONE_NUMBER);
        contact.setId(ID);

        when(repository.findOne(ID)).thenReturn(contact);

        mockMvc.perform(get("/contacts/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.firstName", is(FIRST_NAME)))
                .andExpect(jsonPath("$.lastName", is(LAST_NAME)))
                .andExpect(jsonPath("$.phoneNumber", is(TELEPHONE_NUMBER)));
    }

    @Test
    public void saves_a_valid_contact() throws Exception {
        Contact savedContact = aContact(FIRST_NAME, LAST_NAME, TELEPHONE_NUMBER);
        savedContact.setId(ID);

        Contact contact = aContact(FIRST_NAME, LAST_NAME, TELEPHONE_NUMBER);

        when(repository.save(contact)).thenReturn(savedContact);

        mockMvc.perform(post("/contacts")
                .contentType(APPLICATION_JSON)
                .content(json(contact)))
                .andExpect(header().string("Location", "/contacts/" + ID))
                .andExpect(status().isCreated());
    }

    @Test
    public void does_not_saves_an_invalid_contact() throws Exception {
        verify(repository, never()).save(any(Contact.class));

        Contact invalidContact = new Contact();
        mockMvc.perform(post("/contacts")
                .contentType(APPLICATION_JSON)
                .content(json(invalidContact)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updates_a_valid_contact() throws Exception {
        Contact contact = new Contact();
        contact.setId(ID);
        contact.setFirstName(FIRST_NAME);
        contact.setLastName(LAST_NAME);
        contact.setPhoneNumber(TELEPHONE_NUMBER);

        when(repository.findOne(ID)).thenReturn(contact);

        Contact update = aContact(ANOTHER_FIRST_NAME, LAST_NAME, TELEPHONE_NUMBER);

        mockMvc.perform(put("/contacts/" + ID)
                .contentType(APPLICATION_JSON)
                .content(json(update)))
                .andExpect(status().isNoContent());

        Contact updatedContact = new Contact();
        updatedContact.setId(ID);
        updatedContact.setFirstName(ANOTHER_FIRST_NAME);
        updatedContact.setLastName(LAST_NAME);
        updatedContact.setPhoneNumber(TELEPHONE_NUMBER);

        verify(repository).save(eq(updatedContact));
    }

    @Test
    public void removes_a_contact() throws Exception {
        Contact contact = new Contact();
        contact.setId(ID);
        contact.setFirstName(ANOTHER_FIRST_NAME);
        contact.setLastName(LAST_NAME);
        contact.setPhoneNumber(TELEPHONE_NUMBER);

        when(repository.findOne(ID)).thenReturn(contact);

        mockMvc.perform(delete("/contacts/" + ID))
                .andExpect(status().isNoContent());

        verify(repository).delete(eq(contact));
    }
    
   
    private String json(Object o) throws IOException {
        return new ObjectMapper().writeValueAsString(o);
    }
}
