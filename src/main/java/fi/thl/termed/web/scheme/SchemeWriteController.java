package fi.thl.termed.web.scheme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * SchemeService published as a JSON/REST service.
 */
@RestController
@RequestMapping(value = "/api/schemes")
public class SchemeWriteController {

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Scheme save(@RequestBody Scheme scheme, @AuthenticationPrincipal User user) {
    return schemeService.get(schemeService.save(scheme, user), user)
        .orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "returnIdOnly=true", produces = MediaType.TEXT_PLAIN_VALUE)
  public String saveAndReturnIdOnly(@RequestBody Scheme scheme,
                                    @AuthenticationPrincipal User user) {
    return schemeService.get(schemeService.save(scheme, user), user)
        .orElseThrow(NotFoundException::new).getId().toString();
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody List<Scheme> schemes, @AuthenticationPrincipal User currentUser) {
    schemeService.save(schemes, currentUser);
  }

  @PutJsonMapping(path = "/{schemeId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Scheme save(@PathVariable("schemeId") UUID schemeId,
                     @RequestBody Scheme scheme,
                     @AuthenticationPrincipal User user) {
    scheme.setId(schemeId);
    return schemeService.get(schemeService.save(scheme, user), user)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(path = "/{schemeId}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("schemeId") UUID schemeId,
                     @AuthenticationPrincipal User user) {
    schemeService.delete(new SchemeId(schemeId), user);
  }

}
