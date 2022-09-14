package com.kanpo.trial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.kanpo.trial.service.QuestionTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.Analysis;
import com.kanpo.trial.model.Answer;
import com.kanpo.trial.model.Medicine;
import com.kanpo.trial.model.Question;
import com.kanpo.trial.model.QuestionOption;
import com.kanpo.trial.repository.AnalysisRepository;
import com.kanpo.trial.repository.AnswerRepository;
import com.kanpo.trial.repository.MedicineRepository;
import com.kanpo.trial.repository.QuestionOptionRepository;
import com.kanpo.trial.repository.QuestionRepository;
import com.kanpo.trial.restRequest.SendAnswerRequest;
import com.kanpo.trial.restResponse.NextQuestion;

/**
* RESTのコントローラークラス
* @author　keita
*/
@RestController
public class KanpoRestController {

	@Autowired
	QuestionTreeService questionTreeService;

	/**
	 * /startAnalysis API
	 *
	 * @return TOPの質問を返却します
	 * @throws Exception 例外
	 */
	@GetMapping("/startAnalysis")
	public NextQuestion startAnalysis() throws Exception {
		try {
			return questionTreeService.createAnalysis();
		} catch (Exception e) {
			// 例外が発生した場合はそのままスローしハンドラーに任せる
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /sendAnswer API
	 *
	 * @param request 質問への回答
	 * @return 次の質問を返却します
	 * @throws Exception 例外
	 */
	@PostMapping(value="/sendAnswer")
	public NextQuestion sendAnswer(
			@RequestBody SendAnswerRequest request) throws Exception {

		try {
			// bodyパラメーターログ出力
			request.outputLog();

			// サービスメソッド呼び出し
			return questionTreeService.getNextQuestion(request);

		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /backQuestion APIの処理メソッド
	 *
	 * @param analysisId 解析ID
	 * @return 前の質問を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping(value = "/backQuestion/{analysisId}")
	public NextQuestion backQuestion(@PathVariable long analysisId) throws Exception {
		try {
			return questionTreeService.getPreQuestion(analysisId);
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /getResult APIの処理メソッド
	 *
	 * @param analysisId 解析ID
	 * @return 解析結果を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping(value = "/getResult/{analysisId}")
	public List<Medicine> getResult(@PathVariable long analysisId)  throws Exception {

		try {
			return questionTreeService.getAnalysisResult(analysisId);
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * アプリケーションの初期化メソッド
	 */
	@PostConstruct
	public void init() throws Exception {
		try {
			questionTreeService.createQuestionTree();
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}
}