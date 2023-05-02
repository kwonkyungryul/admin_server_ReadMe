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
import shop.readmecorp.adminserverreadme.modules.book.BookConst;
import shop.readmecorp.adminserverreadme.modules.book.dto.AdminsBookUpdateAndDeleteListDTO;
import shop.readmecorp.adminserverreadme.modules.book.dto.BookDTO;
import shop.readmecorp.adminserverreadme.modules.book.dto.PublishersBookListDTO;
import shop.readmecorp.adminserverreadme.modules.book.entity.Book;
import shop.readmecorp.adminserverreadme.modules.book.entity.Heart;
import shop.readmecorp.adminserverreadme.modules.book.enums.BookStatus;
import shop.readmecorp.adminserverreadme.modules.book.repository.BookRepository;
import shop.readmecorp.adminserverreadme.modules.book.repository.HeartRepository;
import shop.readmecorp.adminserverreadme.modules.book.request.BookSaveRequest;
import shop.readmecorp.adminserverreadme.modules.book.response.BookResponse;
import shop.readmecorp.adminserverreadme.modules.bookdeletelist.BookUpdateListConst;
import shop.readmecorp.adminserverreadme.modules.bookdeletelist.entity.BookDeleteList;
import shop.readmecorp.adminserverreadme.modules.bookdeletelist.enums.BookDeleteListStatus;
import shop.readmecorp.adminserverreadme.modules.bookdeletelist.repository.BookDeleteListRepository;
import shop.readmecorp.adminserverreadme.modules.bookdeletelist.response.BookDeleteListResponse;
import shop.readmecorp.adminserverreadme.modules.bookupdatelist.entity.BookUpdateList;
import shop.readmecorp.adminserverreadme.modules.bookupdatelist.enums.BookUpdateListStatus;
import shop.readmecorp.adminserverreadme.modules.bookupdatelist.repository.BookUpdateListRepository;
import shop.readmecorp.adminserverreadme.modules.bookupdatelist.response.BookUpdateListResponse;
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
import java.util.*;
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
    private final BookUpdateListRepository bookUpdateListRepository;
    private final BookDeleteListRepository bookDeleteListRepository;
    private final ObjectMapper objectMapper;
    private final S3Upload s3Upload;

    public BookService(BookRepository bookRepository, ReviewRepository reviewRepository, PublisherRepository publisherRepository, FileInfoRepository fileInfoRepository, FileRepository fileRepository, HeartRepository heartRepository, BigCategoryRepository bigCategoryRepository, SmallCategoryRepository smallCategoryRepository, BookUpdateListRepository bookUpdateListRepository, BookDeleteListRepository bookDeleteListRepository, ObjectMapper objectMapper, S3Upload s3Upload) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.publisherRepository = publisherRepository;
        this.fileInfoRepository = fileInfoRepository;
        this.fileRepository = fileRepository;
        this.heartRepository = heartRepository;
        this.bigCategoryRepository = bigCategoryRepository;
        this.smallCategoryRepository = smallCategoryRepository;
        this.bookUpdateListRepository = bookUpdateListRepository;
        this.bookDeleteListRepository = bookDeleteListRepository;
        this.objectMapper = objectMapper;
        this.s3Upload = s3Upload;
    }

    public Page<BookDTO> getBookListActive(Pageable pageable) {

        Page<Book> page = bookRepository.findByStatusActive(BookStatus.ACTIVE,pageable);

        List<BookDTO> content = page.getContent()
                .stream()
                .map(book -> {
                    BookDTO bookDTO = book.toDTO();
                    FileInfo fileInfo = book.getFileInfo();

                    List<File> files = fileRepository.findByFileInfo(fileInfo);
                    files.forEach(file -> {
                        String fileName = file.getFileName();
                        if (fileName.endsWith(".epub")) {
                            bookDTO.setEpubUrl(file.getFileUrl());
                        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            bookDTO.setCoverUrl(file.getFileUrl());
                        }
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
                        String fileName = file.getFileName();
                        if (fileName.endsWith(".epub")) {
                            bookDTO.setEpubUrl(file.getFileUrl());
                        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            bookDTO.setCoverUrl(file.getFileUrl());
                        }
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
                        String fileName = file.getFileName();
                        if (fileName.endsWith(".epub")) {
                            bookDTO.setEpubUrl(file.getFileUrl());
                        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            bookDTO.setCoverUrl(file.getFileUrl());
                        }
                    });

                    List<Review> bookReviews = reviewsByBookId.getOrDefault(book.getId(), Collections.emptyList());
                    List<Heart> bookHearts = heartRepository.findByBookId(book.getId()); // 여기에서 book.getId()를 사용합니다.
                    return new PublishersBookListDTO(bookDTO, bookReviews, bookHearts);
                })
                .collect(Collectors.toList());
    }

    public List<BookDTO> getPublishersBookRequest(Integer publisherId) {
        Optional<Publisher> publisher = publisherRepository.findById(publisherId);

        if (publisher.isEmpty()) {
            throw new Exception400(PublisherConst.notFound);
        }

        List<Book> books = bookRepository.findByStatusWaitOrRejected(BookStatus.WAIT, BookStatus.REJECTED, publisherId);

        return books.stream()
                .map(book->{
                    BookDTO bookDTO = book.toDTO();
                    FileInfo fileInfo = book.getFileInfo();

                    List<File> files = fileRepository.findByFileInfo(fileInfo);
                    files.forEach(file -> {
                        String fileName = file.getFileName();
                        if (fileName.endsWith(".epub")) {
                            bookDTO.setEpubUrl(file.getFileUrl());
                        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            bookDTO.setCoverUrl(file.getFileUrl());
                        }
                    });

                    return bookDTO;
                })
                .collect(Collectors.toList());
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
                        String fileName = file.getFileName();
                        if (fileName.endsWith(".epub")) {
                            bookDTO.setEpubUrl(file.getFileUrl());
                        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            bookDTO.setCoverUrl(file.getFileUrl());
                        }
                    });

                    return bookDTO;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public List<AdminsBookUpdateAndDeleteListDTO> getBookUpdateAndDeleteList() {

        List<BookUpdateList> bookUpdateLists = bookUpdateListRepository.findByStatus(BookUpdateListStatus.ACTIVE);
        List<BookDeleteList> bookDeleteLists = bookDeleteListRepository.findByStatus(BookDeleteListStatus.ACTIVE);

        List<AdminsBookUpdateAndDeleteListDTO> dtoList = new ArrayList<>();





        for (BookUpdateList updateList : bookUpdateLists) {

            // 수정요청 하기 전 책표지를 찾기위해서
            String coverUrl = "";
            // 파일정보 불러오기
            FileInfo fileInfo = updateList.getBook().getFileInfo();
            // 파일정보에 있는 파일 불러오기
            List<File> files = fileRepository.findByFileInfo(fileInfo);
            // 파일에 있는 url을 변수에 입력
            for (File file : files) {
                String fileName = file.getFileName();
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    coverUrl = file.getFileUrl();
                }
            }

            AdminsBookUpdateAndDeleteListDTO dto = AdminsBookUpdateAndDeleteListDTO.builder()
                    .id(updateList.getId())
                    .coverUrl(coverUrl)
                    .title(updateList.getBook().getTitle())
                    .author(updateList.getBook().getAuthor())
                    .publisher(updateList.getPublisher().getBusinessName())
//                    .createdDate(updateList.getCreatedDate().toString())
                    .requestType("UPDATE")
                    .status(updateList.getStatus().toString())
                    .build();
            dtoList.add(dto);
        }

        for (BookDeleteList deleteList : bookDeleteLists) {
            AdminsBookUpdateAndDeleteListDTO dto = AdminsBookUpdateAndDeleteListDTO.builder()
                    .id(deleteList.getId())
                    .coverUrl(deleteList.getCoverUrl())
                    .title(deleteList.getBook().getTitle())
                    .author(deleteList.getBook().getAuthor())
                    .publisher(deleteList.getBook().getPublisher().getBusinessName())
                    //TODO 시간
//                    .createdDate(deleteList.getCreatedDate().toString())
                    .requestType("DELETE")
                    .status(deleteList.getStatus().toString())
                    .build();
            dtoList.add(dto);
        }
        return dtoList;
    }

    public BookUpdateListResponse getBookUpdateRequest(Integer id) {

        Optional<BookUpdateList> optionalBookUpdateList = bookUpdateListRepository.findById(id);

        if (optionalBookUpdateList.isEmpty()){
            throw new Exception400(BookUpdateListConst.notFound);
        }

        BookUpdateList bookUpdateList = optionalBookUpdateList.get();
        // 수정요청안에 있는 수정할 책 찾기
        Optional<Book> optionalBook = bookRepository.findById(bookUpdateList.getBook().getId());

        String epubUrl = "";
        String coverUrl = "";

        Book book = optionalBook.get();

        // 파일정보 불러오기
        FileInfo fileInfo = book.getFileInfo();
        // 파일정보에 있는 파일 불러오기
        List<File> files = fileRepository.findByFileInfo(fileInfo);

        // 파일에 있는 url을 변수에 입력
        for (File file : files) {
            String fileName = file.getFileName();
            if (fileName.endsWith(".epub")) {
                epubUrl = file.getFileUrl();
            } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                coverUrl = file.getFileUrl();
            }
        }


        BookUpdateListResponse bookUpdateListResponse = optionalBookUpdateList.get().toResponse(epubUrl, coverUrl);

        return bookUpdateListResponse;
    }

    public BookDeleteListResponse getBookDeleteRequest(Integer id) {

        Optional<BookDeleteList> optionalBookDeleteList = bookDeleteListRepository.findById(id);

        if (optionalBookDeleteList.isEmpty()){
            throw new Exception400(BookUpdateListConst.notFound);
        }

        BookDeleteListResponse bookDeleteListResponse = optionalBookDeleteList.get().toResponse();

        return bookDeleteListResponse;
    }

    public BookResponse getBookWithBookCover(Integer id) {

        var optionalBook = bookRepository.findById(id);
        if (optionalBook.isEmpty()) {
            throw new Exception400(BookConst.notFound);
        }

        Book book = optionalBook.get();
        BookResponse bookResponse = book.toResponse();

        //파일정보 불러오기
        FileInfo fileInfo = book.getFileInfo();

        //파일정보에 있는 파일 불러오기
        List<File> files = fileRepository.findByFileInfo(fileInfo);

        // 파일에 있는 url을 책DTO안 url에 입력
        files.forEach(file -> {
            String fileName = file.getFileName();
            if (fileName.endsWith(".epub")) {
                bookResponse.setEpubUrl(file.getFileUrl());
            } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                bookResponse.setCoverUrl(file.getFileUrl());
            }
        });
        return bookResponse;
    }

    public Optional<Book> getBook(Integer id) {
        var optionalBook = bookRepository.findById(id);
        if (optionalBook.isEmpty()) {
            throw new Exception400(BookConst.notFound);
        }
        return optionalBook;
    }

    @Transactional
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

        // 파일생성 - epub파일
        fileRepository.save(File.builder()
                .fileInfo(epubFileInfo)
                .fileName(epubName)
                .fileUrl(epubUrl)
                .fileSize(epubSize)
                .extension(ParseMultipart.getFileExtension(request.getEpubFile()))
                .status(FileStatus.WAIT)
                .build());
        // 파일생성 - 도서 표지
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

    @Transactional
    public Book update(Integer id) {

        // id로 수정요청 찾기
        Optional<BookUpdateList> optionalBookUpdateList = bookUpdateListRepository.findById(id);

        if (optionalBookUpdateList.isEmpty()) {
            throw new Exception400(BookUpdateListConst.notFound);
        }
        BookUpdateList bookUpdateList = optionalBookUpdateList.get();

        // 수정요청안에 있는 수정할 책 찾기
        Optional<Book> optionalBook = bookRepository.findById(bookUpdateList.getBook().getId());

        if (optionalBook.isEmpty()) {
            throw new Exception400(BookConst.notFound);
        }

        Book book = optionalBook.get();

        // 파일정보 불러오기
        FileInfo fileInfo = book.getFileInfo();

        // 파일정보에 있는 파일 불러오기
        List<File> files = fileRepository.findByFileInfo(fileInfo);

        // 파일에 있는 url 수정
        for (File file : files) {
            String fileName = file.getFileName();
            if (fileName.endsWith(".epub")) {
                file.setFileUrl(bookUpdateList.getEpubUrl());
            } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                file.setFileUrl(bookUpdateList.getCoverUrl());
            }
        }

        // 도서 수정 (수정요청사항을 도서에 덮어쓰기)
        book.setTitle(bookUpdateList.getTitle());
        book.setAuthor(bookUpdateList.getAuthor());
        book.setPrice(bookUpdateList.getPrice());
        book.setIntroduction(bookUpdateList.getIntroduction());
        book.setBigCategory(bookUpdateList.getBigCategory());
        book.setSmallCategory(bookUpdateList.getSmallCategory());
        book.setAuthorInfo(bookUpdateList.getAuthorInfo());

        // 수정요청 상태값 변경
        bookUpdateList.setStatus(BookUpdateListStatus.DELETE);

        return bookRepository.save(book);
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