package shop.readmecorp.adminserverreadme.modules.book.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.readmecorp.adminserverreadme.common.exception.Exception400;
import shop.readmecorp.adminserverreadme.common.util.ParseMultipart;
import shop.readmecorp.adminserverreadme.common.util.S3Upload;
import shop.readmecorp.adminserverreadme.modules.book.dto.BookDTO;
import shop.readmecorp.adminserverreadme.modules.book.dto.PublishersBookListDTO;
import shop.readmecorp.adminserverreadme.modules.book.dto.PublishersBookRequestDTO;
import shop.readmecorp.adminserverreadme.modules.book.entity.Book;
import shop.readmecorp.adminserverreadme.modules.book.entity.Heart;
import shop.readmecorp.adminserverreadme.modules.book.enums.BookStatus;
import shop.readmecorp.adminserverreadme.modules.book.repository.BookRepository;
import shop.readmecorp.adminserverreadme.modules.book.repository.HeartRepository;
import shop.readmecorp.adminserverreadme.modules.book.request.BookSaveRequest;
import shop.readmecorp.adminserverreadme.modules.book.request.BookUpdateRequest;
import shop.readmecorp.adminserverreadme.modules.category.entity.BigCategory;
import shop.readmecorp.adminserverreadme.modules.category.entity.SmallCategory;
import shop.readmecorp.adminserverreadme.modules.category.repository.BigCategoryRepository;
import shop.readmecorp.adminserverreadme.modules.category.repository.SmallCategoryRepository;
import shop.readmecorp.adminserverreadme.modules.file.entity.File;
import shop.readmecorp.adminserverreadme.modules.file.entity.FileInfo;
import shop.readmecorp.adminserverreadme.modules.file.enums.FileStatus;
import shop.readmecorp.adminserverreadme.modules.file.enums.FileType;
import shop.readmecorp.adminserverreadme.modules.file.repository.FileInfoRepository;
import shop.readmecorp.adminserverreadme.modules.file.repository.FileRepository;
import shop.readmecorp.adminserverreadme.modules.publisher.PublisherConst;
import shop.readmecorp.adminserverreadme.modules.publisher.entity.Publisher;
import shop.readmecorp.adminserverreadme.modules.publisher.repository.PublisherRepository;
import shop.readmecorp.adminserverreadme.modules.review.entity.Review;
import shop.readmecorp.adminserverreadme.modules.review.enums.ReviewStatus;
import shop.readmecorp.adminserverreadme.modules.review.repository.ReviewRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final PublisherRepository publisherRepository;
    private final FileInfoRepository fileInfoRepository;
    private final FileRepository fileRepository;
    private final HeartRepository heartRepository;
    private final BigCategoryRepository bigCategoryRepository;
    private final SmallCategoryRepository smallCategoryRepository;
    private final ObjectMapper objectMapper;
    private final S3Upload s3Upload;

    public BookService(BookRepository bookRepository, ReviewRepository reviewRepository, PublisherRepository publisherRepository, FileInfoRepository fileInfoRepository, FileRepository fileRepository, HeartRepository heartRepository, BigCategoryRepository bigCategoryRepository, SmallCategoryRepository smallCategoryRepository, ObjectMapper objectMapper, S3Upload s3Upload) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.publisherRepository = publisherRepository;
        this.fileInfoRepository = fileInfoRepository;
        this.fileRepository = fileRepository;
        this.heartRepository = heartRepository;
        this.bigCategoryRepository = bigCategoryRepository;
        this.smallCategoryRepository = smallCategoryRepository;
        this.objectMapper = objectMapper;
        this.s3Upload = s3Upload;
    }

    public Page<BookDTO> getBookListActive(Pageable pageable) {

//        Page<Book> page = bookRepository.findByStatusActive(BookStatus.ACTIVE,pageable);
//
//        List<BookDTO> content = page.getContent()
//                .stream()
//                .map(Book::toDTO)
//                .collect(Collectors.toList());

        Page<Book> page = bookRepository.findByStatusActive(BookStatus.ACTIVE,pageable);

        List<BookDTO> content = page.getContent()
                .stream()
                .map(book -> {
                    BookDTO bookDTO = book.toDTO();
                    FileInfo fileInfo = book.getFileInfo();

                    List<File> files = fileRepository.findByFileInfo(fileInfo);
                    files.forEach(file -> {
                        bookDTO.setEpubUrl(file.getFileUrl());
                        bookDTO.setCoverUrl(file.getFileUrl());
                    });

                    return bookDTO;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<BookDTO> getBookListActiveOrDelete(Pageable pageable) {

        Page<Book> page = bookRepository.findByStatusActiveOrDelete(BookStatus.ACTIVE,BookStatus.DELETE,pageable);


        List<BookDTO> content = page.getContent()
                .stream()
                .map(book -> {
                    BookDTO bookDTO = book.toDTO();
                    FileInfo fileInfo = book.getFileInfo();

                    List<File> files = fileRepository.findByFileInfo(fileInfo);
                    files.forEach(file -> {
                        bookDTO.setEpubUrl(file.getFileUrl());
                        bookDTO.setCoverUrl(file.getFileUrl());
                    });

                    return bookDTO;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public List<PublishersBookListDTO> getPublishersBookList(Integer publisherId) {
        Optional<Publisher> publisher = publisherRepository.findById(publisherId);

        if (publisher.isEmpty()) {
            throw new Exception400(PublisherConst.notFound);
        }

        List<Book> books = bookRepository.findByPublisherId(publisherId);
        List<Review> reviews = reviewRepository.findByBookPublisherId(publisherId);

        // ACTIVE 리뷰만
        Map<Integer, List<Review>> reviewsByBookId = reviews.stream()
                .filter(review -> review.getStatus().equals(ReviewStatus.ACTIVE))
                .collect(Collectors.groupingBy(review -> review.getBook().getId()));

        return books.stream()
                .map(book -> {
                    BookDTO bookDTO = book.toDTO();

                    //파일정보 불러오기
                    FileInfo fileInfo = book.getFileInfo();

                    //파일정보에 있는 파일 불러오기
                    List<File> files = fileRepository.findByFileInfo(fileInfo);

                    // 파일에 있는 url을 책DTO안 url에 입력
                    files.forEach(file -> {
                        bookDTO.setEpubUrl(file.getFileUrl());
                        bookDTO.setCoverUrl(file.getFileUrl());
                    });

                    List<Review> bookReviews = reviewsByBookId.getOrDefault(book.getId(), Collections.emptyList());
                    List<Heart> bookHearts = heartRepository.findByBookId(book.getId()); // 여기에서 book.getId()를 사용합니다.
                    return new PublishersBookListDTO(bookDTO, bookReviews, bookHearts);
                })
                .collect(Collectors.toList());
    }

    public List<PublishersBookRequestDTO> getPublishersBookRequest(Integer publisherId) {
        Optional<Publisher> publisher = publisherRepository.findById(publisherId);

        if (publisher.isEmpty()) {
            throw new Exception400(PublisherConst.notFound);
        }

        return bookRepository.findByStatusWaitOrRejected(BookStatus.WAIT, BookStatus.REJECTED, publisherId);

    }

    public Page<BookDTO> getBookSaveList(Pageable pageable){

        Page<Book> page = bookRepository.findByStatusWait(BookStatus.WAIT,pageable);

        List<BookDTO> content = page.getContent()
                .stream()
                .map(book -> {
                    BookDTO bookDTO = book.toDTO();
                    FileInfo fileInfo = book.getFileInfo();

                    List<File> files = fileRepository.findByFileInfo(fileInfo);
                    files.forEach(file -> {
                        bookDTO.setEpubUrl(file.getFileUrl());
                        bookDTO.setCoverUrl(file.getFileUrl());
                    });

                    return bookDTO;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Optional<Book> getBook(Integer id) {
        return bookRepository.findById(id);
    }

    public Book save(BookSaveRequest request) {

        Optional<Publisher> optionalPublisher = publisherRepository.findByBusinessName(request.getPublisher());
        Optional<BigCategory> optionalBigCategory = bigCategoryRepository.findByBigCategory(request.getBigCategory());
        Optional<SmallCategory> optionalSmallCategory = smallCategoryRepository.findBySmallCategory(request.getSmallCategory());

        // fileInfo 생성
        FileInfo epubFileInfo = fileInfoRepository.save(FileInfo.builder().type(FileType.BOOK_EPUB).build());
        FileInfo coverFileInfo = fileInfoRepository.save(FileInfo.builder().type(FileType.BOOK_COVER).build());


        String epubUrl = "";
        String coverUrl = "";
        Integer epubSize = 0;
        Integer coverSize = 0;
        ObjectMetadata objMeta = new ObjectMetadata();
        try {
            epubUrl += s3Upload.upload(request.getEpubFile(), "bookepub/");
            coverUrl += s3Upload.upload(request.getBookCover(), "bookcover/");
            epubSize = request.getEpubFile().getInputStream().available();
            coverSize = request.getBookCover().getInputStream().available();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


        // 파일생성에 필요한 fileName, fileUrl 만들기
        String epubName= request.getEpubFile().getOriginalFilename(); // epub 파일이름
        String coverName= request.getBookCover().getOriginalFilename(); // 표지 파일이름

        // 파일생성
        fileRepository.save(File.builder()
                .fileInfo(epubFileInfo)
                .fileName(epubName)
                .fileUrl(epubUrl)
                .fileSize(epubSize)
                .extension(ParseMultipart.getFileExtension(request.getEpubFile()))
                .status(FileStatus.WAIT)
                .build());

        fileRepository.save(File.builder()
                .fileInfo(coverFileInfo)
                .fileName(coverName)
                .fileUrl(coverUrl)
                .fileSize(coverSize)
                .extension(ParseMultipart.getFileExtension(request.getBookCover()))
                .status(FileStatus.WAIT)
                .build());

        // 도서 생성
        Book book = Book.builder()
                .publisher(optionalPublisher.get())
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .introduction(request.getIntroduction())
                .bigCategory(optionalBigCategory.get())
                .smallCategory(optionalSmallCategory.get())
                .authorInfo(request.getAuthorInfo())
                .epub(epubFileInfo)
                .cover(coverFileInfo)
                .status(BookStatus.WAIT)
                .build();

        return bookRepository.save(book);
    }

    public Book update(BookUpdateRequest request, Book book) {
        return null;
    }

    @Transactional
    public Book updateState(String status, Book book) throws Exception {
        Map<String, Object> jsonData = objectMapper.readValue(status, new TypeReference<Map<String, Object>>() {});
        String bookStatus = jsonData.get("status").toString();
        // 책 상태 변경
        book.setStatus(BookStatus.valueOf(bookStatus));
        return bookRepository.save(book);
    }

    public void delete(Book book) {
    }

}