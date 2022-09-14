package com.kanpo.trial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

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

	/** 最初の質問オブジェクトのID */
	public static long topQuestionId = -1;

	@Autowired
	AnalysisRepository analysisRepository;

	@Autowired
	AnswerRepository answerRepository;

	@Autowired
	MedicineRepository medicineRepository;

	@Autowired
	QuestionOptionRepository questionOptionRepository;

	@Autowired
	QuestionRepository questionRepository;

	/**
	 * /startAnalysis APIの処理メソッド
	 *
	 * @return TOPの質問を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping("/startAnalysis")
	public NextQuestion startAnalysis() throws Exception {
		try {
			// TOPの質問を取得する
			Optional<Question> topQuestion = questionRepository.findById(topQuestionId);
			Analysis analysis = new Analysis(topQuestion.get());
			analysisRepository.saveAndFlush(analysis);
			MyLogger.info("Analysis data created successfully analysisId={0}", analysis.getId());

			return new NextQuestion(analysis.getId()
					, analysis.getNowQuestion().getQuestionContent()
					, analysis.getNowQuestion().getOptionList()
					, true);
		} catch (Exception e) {
			// 例外が発生した場合はそのままスローしハンドラーに任せる
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /sendAnswer APIの処理メソッド
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
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(request.analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.END) {
				throw new BadRequestException("Analysis has already been completed");
			}

			// 現在のQuestionオブジェクトを取得する
			Question nowQuestion = analysis.getNowQuestion();
			// QuestionオブジェクトのoptionListと比較して存在しない選択番号が入力された場合はエラー
			if (request.answerNum < 0 || request.answerNum >= nowQuestion.getOptionList().size()) {
				throw new BadRequestException("Illegal answerNum was selected");
			}

			// 解答番号に合致する選択情報を取得する
			QuestionOption queOpt = nowQuestion.getOptionList().get(request.answerNum);
			// 次が質問か解答かをチェックする
			Question nextQuestion = null;
			if (queOpt.getAnswerId() != -1) {
				// answerIdに対応する解答情報を取得する
				Optional<Answer> answerOpt;
				answerOpt = answerRepository.findById(queOpt.getAnswerId());
				if (!answerOpt.isPresent()) {
					throw new InternalServerException("Failed to get answer");
				}
				MyLogger.info("Successfully obtained answer");

				Answer answer = answerOpt.get();
				analysis.setAnswerId(answer.getId());
				analysis.setStatus(Analysis.END);
				analysisRepository.saveAndFlush(analysis);
				return new NextQuestion(analysis.getId(), false);
			}

			// questionIdに対応する次の質問情報を取得する
			Optional<Question> optQue;
			optQue = questionRepository.findById(queOpt.getQuestionId());
			if (!optQue.isPresent()) {
					throw new InternalServerException("Failed to get next question");
			}
			nextQuestion = optQue.get();
			MyLogger.info("Successfully obtained next question");

			// 分析のnowQuestionに返却する質問を設定
			analysis.setNowQuestion(nextQuestion);
			analysisRepository.saveAndFlush(analysis);

			return new NextQuestion(analysis.getId()
					, nextQuestion.getQuestionContent()
					, nextQuestion.getOptionList()
					, true);
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
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.END) {
				throw new BadRequestException("Analysis has already been completed");
			}

			// 現在のQuestionオブジェクトを取得する
			// 前の質問が存在しない場合(例えばトップでの質問など）の場合はエラー
			Question nowQuestion = analysis.getNowQuestion();
			Question backQuestion = nowQuestion.getBackNode();
			if (backQuestion == null) {
				throw new BadRequestException("previous question does not exist");
			}

			// 分析のnowQuestionに返却する質問を設定
			analysis.setNowQuestion(backQuestion);
			analysisRepository.saveAndFlush(analysis);

			return new NextQuestion(analysis.getId()
					, backQuestion.getQuestionContent()
					, backQuestion.getOptionList()
					, true);
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
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.CONTINUE) {
				throw new BadRequestException("Analysis not yet completed");
			}

			// analysisIdからresultIdを取得する
			long answerId = analysis.getAnswerId();
			Optional<Answer> answerOpt = answerRepository.findById(answerId);
			if (!answerOpt.isPresent()) {
				throw new InternalServerException("Failed to get answer");
			}
			return answerOpt.get().getMedicineList();
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * アプリケーションの初期化メソッド
	 */
	@PostConstruct
	public void init() {

		// 漢方一覧を登録する
		ArrayList<Medicine> medicineList = new ArrayList<>();
		Medicine med = new Medicine("葛根湯", "かっこんとう", "風邪の初期に効果的な漢方。\n" +
				"寒気を感じる体を温めて発汗させる効果がある。\n" +
				"寒気がして汗をかいておらず、筋肉にこわばりを感じる時を目安に使用するとよい。", "img01.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("銀翹散", "ぎんぎょうさん", "風邪の初期に効果的な漢方。\n" +
				"喉の奥が痛く、発熱するが寒気がない春夏に多い風邪に良い。\n" +
				"銀翹散は熱を冷まし、炎症を抑える働きがあり、熱邪による風邪の初期に利用する。", "img02.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桂枝湯", "けいしとう", "風邪の初期に効果的な漢方。\n" +
				"桂枝湯は葛根湯よりも発汗させる力が弱い。\n" +
				"すでに汗をかいており、悪寒がある場合に利用する。", "img03.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("麻黄湯", "まおうとう", "風邪の初期に効果的な漢方。\n" +
				"悪寒が強い・汗をかいていない・咳が出るといった場合に適切。\n" +
				"発汗させて余計な水分をとる葛根湯と比べると、葛根湯は「首や肩のこわばり」に向き、麻黄湯は悪寒を伴う関節痛や腰痛に向く。", "img04.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("小青竜湯", "しょうせいりゅうとう", "風邪の初期に効果的な漢方。\n" +
				"透明な鼻水や痰を伴う咳が出る時に利用する。\n" +
				"花粉症の薬としても有名だが長期の利用はおすすめしない。", "img05.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("参蘇飲", "じんそいん", "悪寒あり・汗をかいておらず元気がない・強い倦怠感がある場合に使用する。\n" +
		"寒邪を追い出す働きがあり、水滞を取り除き、胃腸の働きを整える働きが強い薬。胃腸が弱い人で、風邪の初期に元気がない場合に向く。", "img06.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("香蘇散", "こうそさん", "穏やかに発汗を促し、寒邪を追い出す働きがあるが、胃腸の働きをよくする生薬が多く配合されている。\n" +
		"軽い悪寒はあるが、汗をかいておらず、食欲が低下して気持ちが悪い、もしくは精神不安がある場合に適している", "img07.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("防風通聖散", "ぼうふうつうしょうさん", "体内の毒素などをを出して、熱を冷ます薬。\n" +
				"大食い早食い太鼓腹、便秘で志望太りが気になる人にもダイエット用の薬として使われることもある。\n" +
				"熱はあるが、汗は出ない、尿も便もでないという場合に利用。\n" +
				"長期間飲み続けると、体が冷え、体力を消耗してしまうため、長期の利用は避けるべき。", "img08.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("小柴胡湯", "しょうさいことう", "風邪が初期から中期に進行し、発熱と悪寒を繰り返すようになったり、食欲不振や吐き気がある場合に利用する。\n" +
				"風邪以外では、ストレス性の胃炎や咳を抑える効果あり。", "img09.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("柴胡桂枝湯", "さいこけいしとう", "微熱が続く風邪の後半に良い。\n" +
				"吐き気や痛みのあるお腹の風邪に良い。\n" +
				"腸の働きをよくして、気を補う生薬が多く配合されているため、風邪で消耗した体力を補う。", "img10.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("白虎化人参湯", "びゃっこかにんじんとう", "風邪で高熱がでて、激しいのどの渇きや強い倦怠感などがある場合に、強く熱を冷ましつつ、消耗した水を補う。" ,"img11.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("麦門冬湯", "ばくもんどうとう", "風邪が長引いており、のどのイガイガや空咳が出る時に効果的。\n" +
				"のどや口の渇きを抑えてから空咳を沈めたり、痰を着る働きがある。胃を潤して、消化吸収機能を高めるため、風邪で消耗した体力を補う。" ,"img12.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("六君子湯", "りっくんしとう", "四君子湯という代表的なエネルギー補給剤に除湿剤の半夏と陳皮を加えた薬。食欲がない、疲労感が強い、胃が動いている感じがしないというときに向いている。胃痛には向かない。" ,"img13.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("安中散", "あんちゅうさん", "おなかを安らかにさせる古典的な処方の胃腸薬。\n" +
				"胃を温めて痛みをとるため、冷たいものを飲んだり寒い環境にいる時の胃の痛みに向く。" ,"img14.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桂枝加芍薬湯", "けいしかしゃくやくとう", "痛みや痙攣を鎮める働きのある芍薬に、体を温める生薬を加えたもの。冷えによって生じた痙攣を伴うおなかの痛みを改善する" ,"img15.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("小建中湯", "しょうけんちゅうとう", "子供の虚弱体質を改善したり、おなかの痛みを和らげたり、下痢や便秘、おねしょや夜泣きなどに対して幅広く用いられる。\n" +
				"疲れやすく、冷えがあり、便秘または下痢に悩んでいる大人にも使われる。" ,"img16.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("人参湯", "にんじんとう", "エネルギー補給に優れた薬用人参と体を温める乾姜を合わせた薬。胃腸の機能を高め、体を温める作用があるため、冷たいものの飲食で下痢をした場合に効果的。" ,"img17.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("真武湯真武湯", "しんぶとう", "おなかを温めて冷えをとり、余分な水分を取り除くことで下痢を改善する薬。冷えによる下痢やむくみがある人に向く。" ,"img18.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("香正気散 (勝湿顆粒)", "かっこうしょうきさん (しょうしつかりゅう)", "香りの高い生薬で、胃腸の湿気を発散させて働きをよくする。\n" +
				"冷たいものの取りすぎで下痢をしたり、体がだるいという場合や夏バテによる食欲不振、下痢を伴う夏風邪に適する。" ,"img19.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("参苓白朮散料 (健脾散エキス顆粒)", "じんしょうびゃくじゅつさん (けんぴさんえきすかりゅう)", "体内に停滞している余分な湿気を取り除く生薬が多く配合されている。胃腸が弱く、食欲不振や慢性的な下痢のある人に向く。" ,"img20.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("麻子仁丸", "ましにんがん", "胃に潤いを与え、便を柔らかくする働きがある。\n" +
				"コロコロした便が出る時に使う。" ,"img21.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("大黄甘草湯", "だいおうかんぞうとう", "とりあえず便を出したいときに一時的に使用するとよい。\n" +
				"長期的に服用すると体を消耗させるため注意が必要" ,"img22.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桃核承気湯", "とうかくじょうきとう", "血のめぐりが悪いことから引き起こされた体重の増加や、図太りで便秘がある人に用いられる。手足の冷えなどの症状がある人には不向き。" ,"img23.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("大承気湯", "だいじょうきとう", "便秘に加えて、イライラやのぼせ、ほてり、発熱などがある場合に向く。\n" +
				"比較的強い薬のため、継続使用には注意が必要" ,"img24.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("乙字湯", "おつじとう", "ストレスによる肝の疏泄機能が妨げられて便秘になっている場合に使用する。便秘による痔瘻、肛門からの出血がある場合に向く。" ,"img25.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("呉茱萸湯", "ごしゅゆとう", "胃が冷えて、吐き気を伴うような頭痛によく効く。\n" +
				"体を温め、胃腸の調子を改善するのに適している。\n" +
				"のぼせやほてりがある場合は悪化する可能性があるため、注意が必要。" ,"img26.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("川芎茶調散", "せんきゅうちゃちょうさん", "鎮痛作用がある生薬が多くつかわれているため、痛みによく効く。寒い日に外に出ていてゾクゾクする時の頭痛に良い。\n" +
				"頭痛で迷ったらこの漢方を選んでもよい。" ,"img27.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("苓桂朮甘湯", "りょうけいじゅつかんとう", "手足がすごく冷えていて、めまいがする、頭が重いという頭痛に効く。\n" +
				"吐き気を伴う回転性のめまいや乗り物酔いにもよい。\n" +
				"頭の中に余分な水が溜まってむくんでいるような時に温めて水はけを良くし、頭を軽くする。" ,"img28.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桂枝茯苓丸", "けいしぶくりょうがん", "肩こりや首コリから苦しめつけられるような頭痛によい。\n" +
				"さらに激しい月経痛（初日、二日目など）にも使われる薬。" ,"img29.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桂枝加朮附湯", "けいしかじゅつぶとう", "神経痛や関節痛など冷えて痛む、しびれるという人に向く薬。\n" +
				"高齢で腎が弱っている場合は補腎薬が入った独活寄生丸の方が向く場合もある" ,"img30.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("釣藤散", "ちょうとうさん", "高血圧やそれに伴う頭痛、イライラを鎮める。\n" +
				"ストレスや入浴、飲酒により血圧が高くなり、頭痛を伴うめまいがある人に適している。" ,"img31.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("香砂六君子湯", "こうしゃりっくんしとう", "胃腸の動きが悪く、食べ物が詰まって胃が痛む、胃がもたれる、食欲がわかないという場合に利用する。" ,"img32.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("四逆散", "しぎゃくさん", "イライラを伴う胃痛や、ストレスを受けると悪化する胃痛に向く。ストレスを緩和し胃腸を整え、痛みをとる薬のため、イライラや憂鬱間などの情緒不安定に加えておなかが張る、胸苦しい、残便感などの胃腸症状にも" ,"img33.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("芍薬甘草湯", "しゃくやくかんぞうとう", "筋肉の痛みや痙攣を抑える薬。手足や胃腸の痙攣性の痛みに用いる。即効性があるため、足がつった時に飲むと10分程度で治る。" ,"img34.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("独活寄生丸", "どくかつきせいがん", "疲れやすく、冷えると足腰が痛むという場合に向く。" ,"img35.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("疎経活血湯", "そけいかっけつとう", "足腰にビリビリするような痛みがある場合、何かが詰まって血流が悪くなっていると考えられる。そんな腰痛におすすめ。\n" +
				"余分な湿気などのつまりをとり、血流を改善する。同時に補血する作用もあり。" ,"img36.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("補中益気湯", "ほちゅうえつきとう", "疲労、起き上がれない、気力がない、元気がないというときにおすすめ。" ,"img37.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("十全大補湯", "じゅうぜんだいほとう", "疲れに加えて、顔色が白く、頭が働かない、冷えて眠れないなどの血虚の症状があり、おなかの調子もよくない時に向く。" ,"img38.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("帰脾湯", "きひとう", "疲れがひどく、元気がない、頭が働かない、ぼーっとするなどの症状があり、不安感や睡眠の不調がある場合におすすめ。\n" +
				"四君子湯に気血を増強する生薬や、精神を安定させる生薬を加えた薬。" ,"img39.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("四物湯", "しもつとう", "血を増やす漢方薬の中で、最もベーシックな処方。\n" +
				"疲れやすい、頭がぼーっとする、冷える、肌や紙につやがなく乾燥するなどの血虚の症状があるときに向く。", "img40.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("生脈散", "しょうみゃくさん", "たくさん汗をかき、疲れそうなときに前もって使うのがおすすめ。予防的に使うのが最も効果的。夏バテにも適している。", "img41.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("清暑益気湯", "せいしょえっきとう", "生脈散に少し熱を冷ます生薬がたされたもの。\n" +
				"夏場の発汗による脱水と疲労に使う。\n" +
				"おなかの調子をよくする作用もある。", "img42.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("逍遥散", "しょうようさん", "憂鬱間や怒りっぽいなどの情緒不安定や腹痛、おなかの張りなどの症状に加え、頭がぼーっとする、ふらつく、目が疲れる、つかれやすいなどの血虚の症状があるときに使う。", "img43.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("加味逍遙散", "かみしょうようさん", "月経前になると、ホルモンバランスに乱れが生じて不調になる人が最初に試すとよい薬。\n" +
				"排卵痛やイライラ、さらに肩こり、疲れやのぼせなどを排卵期から月経前に感じて、気分がすっきりしない状態に特に用いられる。", "img44.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("加味帰脾湯", "かみきひとう", "食欲も元気もなく、疲れやすい、眠りが浅い、夢を見る、不安感が強いという場合につかわれる。", "img45.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("酸棗仁湯", "さんそうにんとう", "心身が疲れているのに眠れない、もしくは眠りが浅いときに使う。漢方の睡眠薬として処方される。", "img46.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("半夏厚朴湯", "はんげこうぼくとう", "ストレスによる喉のつまりや吐き気感を取り除く薬。\n" +
				"長期的に使うと感想が助長されて精神が不安定になりやすいのでお勧めしない。", "img47.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("柴胡加竜骨牡蠣湯", "さいこかりゅうこつぼれいとう", "不安、焦り、不眠、動悸など興奮した状態を鎮める薬。\n" +
				"下剤成分がはいっているため、便秘がある人にも良い。\n" +
				"比較的体力がある人でストレスを強く感じている人に良い。", "img48.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("桂枝加竜骨牡蛎湯", "けいしかりゅうこつぼれいとう", "体は冷えているのに頭は興奮しているような人に良い。体力が衰えており、脳が軽微な刺激に反応していしまう状態のため、精神を安定させる生薬がベース。精神安定剤。", "img49.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("抑肝散", "よくかんさん", "ストレスがうまく発散できない人に良い。\n" +
				"小児のひきつけや夜泣き、チック症状に対して使われる薬。大人のイライラ、緊張、うつ状態、手足の震え、筋肉の痙攣、歯ぎしり薬石張りなどの改善にも使われる。", "img50.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("当帰芍薬散", "とうきしゃくやくさん", "月経期間が短くなる、経血量が少なくなるなどの月経不順、貧血や疲労倦怠感、冷えやむくみなどに良い。\n" +
				"妊婦、流産による障害など、幅広く用いられる。", "img51.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("婦宝当帰膠", "ふほうとうきこう", "血を増やすベーシックな処方である四物湯にさらに血や潤いを補う生薬をプラスした薬。血虚によるめまい、立ちくるみ、髪や爪の不調、頭痛、月経痛などによい。", "img52.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("血府逐於丸", "けつぷちくおがん", "代表的な血流改善薬。ストレスを伴う月経痛と頭痛があったらまずは検討する。", "img53.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("折衝飲", "せつしょういん", "血流改善薬で、強い痛み止めが入っている。\n" +
				"痛みがあるときはまずはこれを試してみるとよい。", "img54.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("芎帰調血飲第一加減 ", "きゅうきちょうけついんだいいちかげん", "元気がなく、疲れやすい時に使う。\n" +
				"産後の腰痛や神経痛、起立不全、産後鬱などにも向く。", "img55.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("六味丸", "ろくみがん", "補腎剤である八味地黄丸から体を温める生薬を抜いたもの。\n" +
				"月経不順や子供の発育不全にも良いとされる。", "img56.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("知柏地黄丸", "ちばくじおうがん", "腎に潤い鵜を補給する六味丸に、体にこもった熱をとる生薬をプラスした薬。六味丸では熱が取れない場合に向く。", "img57.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("小菊地黄丸", "こぎくじおうがん", "六味丸に目の働きを改善する生薬をプラスした薬。\n" +
				"「飲む目薬」と呼ばれる。", "img58.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("八味地黄丸", "はちみじおうがん", "加齢とともに弱る腎の動きを改善する薬。\n" +
				"足腰が冷える、腰痛、頻尿、歩行困難の改善に役立つ。", "img59.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("防己黄耆湯", "ぼういおうぎとう", "体重の割には体脂肪が多く、ぷよぷよした水太り、下半身太りの解消に適している。\n" +
				"ぽっちゃりしていて疲れ気味、むくみがある、だるい、汗をかきやすいという人に向く薬。体内の余分な水分をとって、巡らせる働きや、気を補って汗が漏れ出るのを防ぐ働きがある。", "img60.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("牛車腎気丸", "ごしゃじんきがん", "補腎剤である八味地黄丸に水はけの力をプラスした薬。\n" +
				"体に冷えがあり疲れやすく、腰から下のむくみやだるさ、ひざの痛みなどを感じている人に用いられる薬。", "img61.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("猪苓湯", "ちょれいとう", "水はけが悪く、尿路に炎症や痛みがある場合に利尿作用を持たせて炎症を鎮める。微熱やほてり、のどの渇き、脱水傾向がある時や膀胱炎に良い。", "img62.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("竜胆瀉肝湯", "りゅうたんしゃかんとう", "下腹部の熱間や痛みをとるため、膀胱炎の残尿痛や残尿感があるときなど尿路のトラブルがある場合に。高血圧症の頭痛、ふらつき、のぼせ、イライラなどにもよく使われる。", "img63.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("荊芥連翹湯", "けいがいれんぎょうとう", "鼻詰まりや花粉症のアレルギー症状に悩んでいるときに良い。\n" +
				"呼吸器や粘膜の炎症に用いられる薬。", "img64.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("半夏白朮天麻湯", "はんげびゃくじゅつてんまとう", "低気圧が近づいたり、雨の日、湿度のある日に頭が重く痛む、さらに回転性のめまいを伴うようなときに用いられる。", "img65.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("五苓散", "ごれいさん", "頭が重くて痛い・吐き気・むくみのある二日酔いに最適。\n" +
				"利水効果を高めつつ、胃腸を正常化させて、体を温めるのによい。", "img66.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		med = new Medicine("黄連解毒湯", "おうれんげどくとう", "胸やけ、食べ過ぎ、口内炎や口臭のある二日酔いに最適。\n" +
				"スパイシーなものや油もの、こってりとして味のものを食べつつ、飲みすぎて二日酔いになった時の薬。", "img67.jpeg");
		medicineRepository.saveAndFlush(med);
		medicineList.add(med);

		// 解答を登録する
		ArrayList<Answer> answerList = new ArrayList<>();
		Answer ans = null;
		ArrayList<Medicine> ansMedicineList = null;

		/* 風邪の初期の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(2));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(0));
		ansMedicineList.add(medicineList.get(3));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(3));
		ansMedicineList.add(medicineList.get(0));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(6));
		ansMedicineList.add(medicineList.get(0));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(5));
		ansMedicineList.add(medicineList.get(0));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(7));
		ansMedicineList.add(medicineList.get(1));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(1));
		ansMedicineList.add(medicineList.get(7));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(4));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 風邪の中期の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(8));
		ansMedicineList.add(medicineList.get(9));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(9));
		ansMedicineList.add(medicineList.get(8));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(10));
		ansMedicineList.add(medicineList.get(8));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 風邪の後期の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(9));
		ansMedicineList.add(medicineList.get(11));
		ansMedicineList.add(medicineList.get(37));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(11));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(37));
		ansMedicineList.add(medicineList.get(36));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 胃腸の不調の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(12));
		ansMedicineList.add(medicineList.get(18));
		ansMedicineList.add(medicineList.get(19));
		ansMedicineList.add(medicineList.get(25));
		ansMedicineList.add(medicineList.get(31));
		ansMedicineList.add(medicineList.get(41));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(13));
		ansMedicineList.add(medicineList.get(14));
		ansMedicineList.add(medicineList.get(32));
		ansMedicineList.add(medicineList.get(33));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(15));
		ansMedicineList.add(medicineList.get(16));
		ansMedicineList.add(medicineList.get(17));
		ansMedicineList.add(medicineList.get(18));
		ansMedicineList.add(medicineList.get(19));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(15));
		ansMedicineList.add(medicineList.get(20));
		ansMedicineList.add(medicineList.get(21));
		ansMedicineList.add(medicineList.get(22));
		ansMedicineList.add(medicineList.get(23));
		ansMedicineList.add(medicineList.get(24));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 精神不安の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(30));
		ansMedicineList.add(medicineList.get(32));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(43));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(44));
		ansMedicineList.add(medicineList.get(47));
		ansMedicineList.add(medicineList.get(48));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(46));
		ansMedicineList.add(medicineList.get(49));
		ansMedicineList.add(medicineList.get(52));
		ansMedicineList.add(medicineList.get(42));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 疲労の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(34));
		ansMedicineList.add(medicineList.get(40));
		ansMedicineList.add(medicineList.get(54));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(36));
		ansMedicineList.add(medicineList.get(37));
		ansMedicineList.add(medicineList.get(38));
		ansMedicineList.add(medicineList.get(54));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(38));
		ansMedicineList.add(medicineList.get(44));
		ansMedicineList.add(medicineList.get(45));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(39));
		ansMedicineList.add(medicineList.get(37));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 女性不調の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(22));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(28));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(28));
		ansMedicineList.add(medicineList.get(52));
		ansMedicineList.add(medicineList.get(53));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(43));
		ansMedicineList.add(medicineList.get(50));
		ansMedicineList.add(medicineList.get(52));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(51));
		ansMedicineList.add(medicineList.get(52));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(55));
		ansMedicineList.add(medicineList.get(50));
		ansMedicineList.add(medicineList.get(56));
		ansMedicineList.add(medicineList.get(57));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(54));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* 頭痛の解答を設定する */
		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(25));
		ansMedicineList.add(medicineList.get(26));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(27));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(28));
		ansMedicineList.add(medicineList.get(64));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ans = new Answer();
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(30));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/* その他の解答を設定する */
		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(4));
		ansMedicineList.add(medicineList.get(63));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(7));
		ansMedicineList.add(medicineList.get(59));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(8));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(18));
		ansMedicineList.add(medicineList.get(40));
		ansMedicineList.add(medicineList.get(41));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(29));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(33));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(38));
		ansMedicineList.add(medicineList.get(45));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(57));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(58));
		ansMedicineList.add(medicineList.get(60));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(61));
		ansMedicineList.add(medicineList.get(62));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		ansMedicineList = new ArrayList<>();
		ansMedicineList.add(medicineList.get(65));
		ansMedicineList.add(medicineList.get(66));
		ans.setMedicineList(ansMedicineList);
		answerRepository.saveAndFlush(ans);
		answerList.add(ans);

		/*　質問のツリーを作成する */
		Question que = null;
		QuestionOption queop = null;
		ArrayList<QuestionOption> optionList = null;
		ArrayList<Question> questionList = new ArrayList<>();

		/* 風邪の初期の質問ツリーを形成 */
		// 質問0
		optionList = new ArrayList<>();
		queop = new QuestionOption("首、筋肉がこわばる", answerList.get(1));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("関節痛がある", answerList.get(2));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("強い倦怠感", answerList.get(3));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("吐き気がある", answerList.get(4));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下で当てはまるものを選んでください");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		// 質問1
		optionList = new ArrayList<>();
		queop = new QuestionOption("汗をかいている", answerList.get(0));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("汗をかいていない", questionList.get(0));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("今汗をかいていますか？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		// 質問2
		optionList = new ArrayList<>();
		queop = new QuestionOption("便秘症状がある", answerList.get(5));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("便秘症状がない", answerList.get(6));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("便秘症状がありますか？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		//　質問3
		optionList = new ArrayList<>();
		queop = new QuestionOption("寒気がある", questionList.get(1));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("発熱、喉の痛み", questionList.get(2));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("鼻水", answerList.get(7));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("症状がどれが強いですか？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 風邪の中期の質問ツリーを形成 */
		// 質問4
		optionList = new ArrayList<>();
		queop = new QuestionOption("食欲不振、吐き気", answerList.get(8));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("激しい喉の渇きや強い倦怠感", answerList.get(9));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("どの症状が強いか？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		// 質問5
		optionList = new ArrayList<>();
		queop = new QuestionOption("高熱(38度〜)", questionList.get(4));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("微熱(37度台)", answerList.get(10));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("現在の体温は？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 風邪の後期の質問ツリーを形成 */
		// 質問6
		optionList = new ArrayList<>();
		queop = new QuestionOption("吐き気やお腹に痛み", answerList.get(11));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("喉のイガイガや空咳が長引く", answerList.get(12));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("疲労、元気が出ない", answerList.get(13));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものを選んでください");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 胃腸の不調の質問ツリーを形成 */
		// 質問7
		optionList = new ArrayList<>();
		queop = new QuestionOption("胃腸の働きが弱い", answerList.get(14));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("胃腸の痛み", answerList.get(15));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("下痢", answerList.get(16));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("便秘", answerList.get(17));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものを選んでください");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 精神不安(ストレス)の質問ツリーを形成 */
		//質問8
		optionList = new ArrayList<>();
		queop = new QuestionOption("高血圧やイライラ", answerList.get(18));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("月経前のホルモンバランスの乱れ", answerList.get(19));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("興奮/不安感が強く不眠", answerList.get(20));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("ストレスによる体調不良", answerList.get(21));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものはどれか");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 疲労の質問ツリーを形成 */
		// 質問9
		optionList = new ArrayList<>();
		queop = new QuestionOption("疲れやすい", answerList.get(22));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("気力・元気が出ない", answerList.get(23));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("疲れによる不眠", answerList.get(24));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("血が足りない(血虚)", answerList.get(25));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものはどれか");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 女性の不調（月経など)の質問ツリーを形成 */
		// 質問10
		optionList = new ArrayList<>();
		queop = new QuestionOption("体調の増加や便秘", answerList.get(26));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("肩や首のこり", answerList.get(27));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("激しい月経痛", answerList.get(28));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("月経前のホルモンバランスの乱れ", answerList.get(29));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("血が足りない(血虚)", answerList.get(30));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("月経不順", answerList.get(31));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("産後の不調", answerList.get(32));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものはどれか");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* 頭痛の質問ツリーを形成 */
		// 質問11
		optionList = new ArrayList<>();
		queop = new QuestionOption("冷えからくる頭痛", answerList.get(33));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("乗り物酔い（めまい)", answerList.get(34));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("肩首のこりから頭痛", answerList.get(35));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("高血圧に伴う頭痛", answerList.get(36));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものはどれか");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* その他の質問ツリーを形成 */
		// 質問12
		optionList = new ArrayList<>();
		queop = new QuestionOption("花粉症", answerList.get(37));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("肥満による不調", answerList.get(38));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("胃炎", answerList.get(39));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("夏バテ", answerList.get(40));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("神経痛・関節痛", answerList.get(41));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("痙攣症状(足がつるなど)", answerList.get(42));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("不眠", answerList.get(43));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("疲れ目", answerList.get(44));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("老化", answerList.get(45));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("尿路のトラブル", answerList.get(46));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("二日酔い", answerList.get(47));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("以下の症状で近いものはどれか");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);

		/* トップの質問 */
		// 質問13
		optionList = new ArrayList<>();
		queop = new QuestionOption("風邪の初期", questionList.get(1));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("風邪の中期", questionList.get(5));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("風邪の後期", questionList.get(6));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("胃腸の不調", questionList.get(7));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("精神不安(ストレス)", questionList.get(8));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("女性の不調(生理痛など)", questionList.get(10));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("頭痛", questionList.get(11));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);
		queop = new QuestionOption("その他の不調", questionList.get(12));
		questionOptionRepository.saveAndFlush(queop);
		optionList.add(queop);

		que = new Question();
		que.setQuestionContent("あなたのお悩みに近い項目はどれですか？");
		que.setOptionList(optionList);
		questionRepository.saveAndFlush(que);
		questionList.add(que);
		// トップ質問を設定する
		topQuestionId = que.getId();

		/* 親ノードの設定 */
		// 質問0の親ノードは質問1
		questionList.get(0).setBackNode(questionList.get(1));
		questionRepository.saveAndFlush(questionList.get(0));
		// 質問1の親ノードは質問3
		questionList.get(1).setBackNode(questionList.get(3));
		questionRepository.saveAndFlush(questionList.get(1));
		// 質問2の親ノードは質問3
		questionList.get(2).setBackNode(questionList.get(3));
		questionRepository.saveAndFlush(questionList.get(2));
		// 質問3の親ノードはトップ質問
		questionList.get(3).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(3));
		// 質問4の親ノードは質問5
		questionList.get(4).setBackNode(questionList.get(5));
		questionRepository.saveAndFlush(questionList.get(4));
		// 質問5の親ノードはトップ質問
		questionList.get(5).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(5));
		// 質問6の親ノードはトップ質問
		questionList.get(6).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(6));
		// 質問7の親ノードはトップ質問
		questionList.get(7).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(7));
		// 質問8の親ノードはトップ質問
		questionList.get(8).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(8));
		// 質問9の親ノードはトップ質問
		questionList.get(9).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(9));
		// 質問10の親ノードはトップ質問
		questionList.get(10).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(10));
		// 質問11の親ノードはトップ質問
		questionList.get(11).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(11));
		// 質問12の親ノードはトップ質問
		questionList.get(12).setBackNode(questionList.get(13));
		questionRepository.saveAndFlush(questionList.get(12));

		return;
	}
}