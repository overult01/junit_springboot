package studio.thinkground.aroundhub.controller;

// 코드의 길이를 짧게 하기 위해 ststic import 
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import studio.thinkground.aroundhub.data.dto.ProductDto;
import studio.thinkground.aroundhub.service.impl.ProductServiceImpl;

@WebMvcTest(ProductController.class) // 스프링의 모든 빈을 로드하여 테스트하는 방식인 @SpringBootTest어노테이션 대신 컨트롤러 관련 코드만 테스트하고자 할때 사용하는 어노테이션
// @AutoConfigureWebMvc // 이 어노테이션을 통해 MockMvc를 Builder 없이 주입받을 수 있음
public class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  // ProductController에서 잡고 있는 Bean 객체에 대해 Mock 형태의 객체를 생성해줌
  @MockBean
  ProductServiceImpl productService;

  // http://localhost:8080/api/v1/product-api/product/{productId}
  @Test // 테스트 1개의 주체 
  @DisplayName("Product 데이터 가져오기 테스트") // 테스트에 대한 설명 
  void getProductTest() throws Exception {

	// mockito 라이브러리: mock 객체의 생성, 사용에 도움 
    // given (~와 같은 상황이 주어졌을 때): Mock 객체가 특정 상황에서 해야하는 행위를 정의하는 메소드
    given(productService.getProduct("12315")).willReturn( // given안의 메서드가 실행될 때, wiiReturn뒤의 값이 리턴될거다. 여기선 getProduct메서드자체가 ProductDto객체를 리턴하기 때문에 이렇게 리턴.
        new ProductDto("15871", "pen", 5000, 2000)); 

    String productId = "12315";

    // builder 구조로 .으로 연결  
    mockMvc.perform( // mockMvc.perform 메서드: rest api 테스트 환경 제공 
            get("/api/v1/product-api/product/" + productId)) // get 통신방식 지정. 
        .andExpect(status().isOk())     // andExpect : 기대하는 값이 나왔는지 체크해볼 수 있는 메소드
        .andExpect(jsonPath("$.productId").exists()) // http request를 날렸을 때 전달받은 json형태의 body값이 기대한 대로인지 확인. json path의 depth가 깊어지면 .을 추가하여 탐색할 수 있음 (ex : $.productId.productIdName)
        .andExpect(jsonPath("$.productName").exists()) // $.json의 key값 
        .andExpect(jsonPath("$.productPrice").exists())
        .andExpect(jsonPath("$.productStock").exists())
        .andDo(print()); // andDo: 테스트 내용을 print 

    // verify : 해당 객체의 메소드가 실행되었는지 체크해줌
    verify(productService).getProduct("12315");
  }


  // http://localhost:8080/api/v1/product-api/product
  @Test
  @DisplayName("Product 데이터 생성 테스트")
  void createProductTest() throws Exception {
	// mockito 라이브러리: mock객체 
    //Mock 객체에서 특정 메소드가 실행되는 경우 실제 Return을 줄 수 없기 때문에 아래와 같이 실행상황을 가정하여 리턴값 체크 
    given(productService.saveProduct("15871", "pen", 5000, 2000)).willReturn(
        new ProductDto("15871", "pen", 5000, 2000));

    ProductDto productDto = ProductDto.builder().productId("15871").productName("pen")
        .productPrice(5000).productStock(2000).build(); // builder
    Gson gson = new Gson(); // gson: json를 자유롭게 다룰 수 있도록 편의를 제공한 라이브러리 
    String content = gson.toJson(productDto);// gson.toJson메서드: productDto 객체를 json 형태로 변경 

    // 아래 코드로 json 형태 변경 작업을 대체할 수 있음
    // String json = new ObjectMapper().writeValueAsString(productDto);

    mockMvc.perform(
            post("/api/v1/product-api/product") //post 방식으로 통신 
                .content(content) // mockMvc.perform.content: 바디값 넘겨줄 내용 전달  
                .contentType(MediaType.APPLICATION_JSON)) // .contentType: 타입지정. 일반적으로 json 형태사용시 APPLICATION_JSON 사용
    	// .andExpect: 요청에 대한 기대값 확인 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").exists())
        .andExpect(jsonPath("$.productName").exists())
        .andExpect(jsonPath("$.productPrice").exists())
        .andExpect(jsonPath("$.productStock").exists())
        .andDo(print());

    verify(productService).saveProduct("15871", "pen", 5000, 2000);
  }

}
