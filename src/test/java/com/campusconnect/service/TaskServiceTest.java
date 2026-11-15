package com.campusconnect.service;

import com.campusconnect.campus.Model.Task;
import com.campusconnect.campus.Repositories.TaskRepository;
import com.campusconnect.campus.Service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Test // is method ko test ke roop mein execute kro -->means java is method ko nahi chalata hain
    // JUNIT -->ko signal milta hain ye ek normal method nahi bliki yeh ek test case ki trh treat kro
    // test case means joh hme expected result chahiye woh mil rha hain kya ?? ye hain iska mtlb
    public void exampleTest(){
       System.out.println("hello task service ");
    }
    // upar walle method ko JUNIT run krta hain rather than -->java krta hain isse as a test case dekha jaata hain


  // NOW understand assertEquals method how it works like -->ismein hm expected and actual hoti hain and JUNIT
  // SE CHECK KRTE HAIN KII KI EXPECTED AND JOH ACTUAL NIKLI WOH SAME HAIN YAA NAHI
    @Test
    public void exampleTest2(){
        int actual=10;
        int expected=10;
        assertEquals(actual,expected);
    }

    // INTRODUCING MOCKITO CONCEPT --->VERY IMPORTANT !!
    // USUALLY REAL MEI JB ACTUAL SERVICE MEIN KAAM KRTE HAIN TOH USMEIN REAL DATABASE SE INTERACTION KRTE HAIN BUT
    // UNIT TESTING KA KAAM HOTA HAI PRIMARY JOH YE SERVICE MEIN LOGIC LIKHA HAIN WOH ACTUALLY MEIN KAAM KR BHI RHA HAIN
    // TOH KUCH SERVICES HOTI HAIN JHA HME REPSOTIORIES CHAHIYE HOTI HAIN WHERE WE NEED DATA FROM DATABASE BUT UNIT
    // RTESTING MEIN HM NAHI CHAHENGE KI REAL DATABSE SE BAAT KRE INSTEAD HM MOCKITO KA USE KRTE HAIN -->WOH KYA KRTA HA
    /* HAIN ISMEIN ?? ==>ISMEIN REAL DEPENDENCY MT DO WOH CONTROLLED DEPENDENCY KA FAKE OBJECT BNA DETA HOON -->ISSE
    * HOGA KYA REAL REPOSITORY NAHI BNTA BLKI MOCK TASKsERVICE REPOSITORY bnayega  and woh hamare real database se
    * coonect bhi nahi hoga isse real data ko touch bhi nahi krega AB!! isse hoga kya??==>ab maan le task service maange
    * musje taskID1 chahiye toh usse task woh de dena kyuki yaha logic check hoon rha hai rather than database ki wroking
    *  */

    /*when(taskRepository.findById(1L))
    .thenReturn(Optional.of(task)); ===>aisa likha hoon tb kya krna hain hota hain ismein ye likha agr koi findById se
    call kre toh real database mein mt jaana blki simply task return kr dena service ko */

    @Mock
    private TaskRepository taskRepository;// mockito runtime mein fake object bna dega iska
    @Test
    public void  testExample3(){
        when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task()));
        // here when and thenReturn are not actual service instead of this we are giving isntructions to Mockito
        // that if you find that -->that agr hm usse kuch de woh hme kya return krega woh bhi hm hii decide krenge
        /* upar walle line kam mtlb simple hain -->Mockito, agar mere fake taskRepository par findById(1L) call ho,
        toh database se kuch mat lena. new Task() return kar dena.*/
        Optional<Task> task=taskRepository.findById(1L);
        assertEquals(true,task.isPresent()); // it actualls verifies ki task actual mein return hua hain yaa nahi
    }

}

