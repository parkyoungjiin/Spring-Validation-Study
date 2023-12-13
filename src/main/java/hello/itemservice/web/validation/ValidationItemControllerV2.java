package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        // 컨트롤러 호출될 때 마다 검증기를 적용
        dataBinder.addValidators(itemValidator);
    }
    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        //검증 로직 (Item 객체에 글자가 없다면 errors라는 Map 객체에 에러를 저장.)
        if (!StringUtils.hasText(item.getItemName())) {
//            errors.put("itemName", "상품 이름은 필수입니다.");
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
//            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }


        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000){
//                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
                // field 에러가 아닌, global 에러이기에 ObjectError로
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 =" + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", errors);
            //model를 담지 않아도 BindingResult가 모델을 담는다.
//            model.addAttribute("errors", errors);
            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }
//V2 : rejectedValue를 사용한 모델, errors & code는 사용하지 않음.
//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        //검증 로직 (Item 객체에 글자가 없다면 errors라는 Map 객체에 에러를 저장.)
        if (!StringUtils.hasText(item.getItemName())) {
//            errors.put("itemName", "상품 이름은 필수입니다.");
            bindingResult.addError(new FieldError("item", "itemName",item.getItemName(),false,null,null,"상품 이름은 필수 입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "price",item.getPrice(),false,null,null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
//            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(),false,null,null,"수량은 최대 9,999 까지 허용합니다."));
        }


        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000){
//                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
                // field 에러가 아닌, global 에러이기에 ObjectError로
                bindingResult.addError(new ObjectError("item",null,null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 =" + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", errors);
            //model를 담지 않아도 BindingResult가 모델을 담는다.
//            model.addAttribute("errors", errors);
            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //V3 : rejected value + errors & code를 적용한 코드.
//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        //검증 로직 (Item 객체에 글자가 없다면 errors라는 Map 객체에 에러를 저장.)
        if (!StringUtils.hasText(item.getItemName())) {
//            errors.put("itemName", "상품 이름은 필수입니다.");
            bindingResult.addError(new FieldError("item", "itemName",item.getItemName(),false, new String[]{"required.item.itemName", "required.default"},null,null));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "price",item.getPrice(),false,new String[]{"range.item.price"},new Object[]{1000, 1000000}, null));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
//            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(),false,new String[]{"max.item.quantity"},new Object[]{9999},null));
        }


        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000){
//                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
                // field 에러가 아닌, global 에러이기에 ObjectError로
                bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"},new Object[]{10000,resultPrice}, null));
            }
        }

        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", errors);
            //model를 담지 않아도 BindingResult가 모델을 담는다.
//            model.addAttribute("errors", errors);
            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //V4 : rejectValue를 사용(MessageCodesResolver아 자동으로 메시지를 생성)
//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if(bindingResult.hasErrors()){
            log.info("errors={}", bindingResult);
            return "validation/v2/addForm";
        }

        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        //검증 로직 (Item 객체에 글자가 없다면 errors라는 Map 객체에 에러를 저장.)
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range",new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }


        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000){
                bindingResult.reject("totalPriceMin");
            }
        }

        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //V5 :  검증하는 로직을 validate 메서드에 분리하였음. (코드가 굉장히 간결해짐.)

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        itemValidator.validate(item, bindingResult);

        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //V6 :  @Validated를 사용하여 자동 검증

    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증에 실패하면 다시 입력 폼으로(뷰 템플릿으로)
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "validation/v2/addForm";
        }

        //검증에 걸리지 않고 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

