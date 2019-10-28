package com.yushkevich.watermark.controller;

import com.yushkevich.watermark.domain.Content;
import com.yushkevich.watermark.domain.Publication;
import com.yushkevich.watermark.dto.PageDTO;
import com.yushkevich.watermark.dto.PublicationDTO;
import com.yushkevich.watermark.dto.PublicationRequestDTO;
import com.yushkevich.watermark.service.PublicationService;
import com.yushkevich.watermark.validation.PublicationDTOValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import static com.yushkevich.watermark.utils.mapper.PublicationMapper.*;

@RestController
@RequestMapping("/api/v1/publication")
@AllArgsConstructor
@Slf4j
@Api("Sync CRUD operations for book and journals")
public class PublicationController {

    private final PublicationService publicationService;
    private final PublicationDTOValidator validator;

    @InitBinder("publicationDTO")
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Creates publication.")
    public PublicationDTO createPublication(@Validated @RequestBody PublicationDTO request) {
        log.debug("::createPublication {}", request);

        final Publication publication = publicationService.create(buildPublication(request));

        return buildPublicationDTO(publication);
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET, params = {"content!="})
    @ApiOperation(value = "Fetches list of publication per page.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. " +
                            "Multiple sort criteria are supported.")
    })
    public PageDTO<PublicationDTO> listPublications(Pageable pageable, @RequestParam Content content) {
        log.debug("::listPublications {} with {} content", pageable, content);

        return buildPagePublicationDTO(publicationService.findAllByPage(pageable, content));
    }

    @RequestMapping(path = "/{publication_id}", method = RequestMethod.GET, params = {"content!="})
    @ApiOperation(value = "Finds publication by id.")
    public PublicationDTO getPublicationById(@PathVariable("publication_id") Long id, @RequestParam Content content) {
        log.debug("::getById {} with {} content", id, content);

        return buildPublicationDTO(publicationService.find(id, content));
    }

    @RequestMapping(path = "/update", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Updates publication.")
    public PublicationDTO updatePublication(@RequestBody PublicationDTO request) {
        log.debug("::updatePublication {}", request);

        final Publication publication = publicationService.update(buildPublication(request));

        return buildPublicationDTO(publication);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Deletes publication.")
    public void deletePublication(@Validated @RequestBody PublicationRequestDTO request) {
        log.debug("::deletePublication {}", request);

        publicationService.delete(request.getPublicationId(), request.getContent());
    }
}
