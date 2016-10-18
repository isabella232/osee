package org.eclipse.osee.framework.core.dsl.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.osee.framework.core.dsl.services.OseeDslGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalOseeDslParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_WHOLE_NUM_STR", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'import'", "'.'", "'abstract'", "'artifactType'", "'extends'", "','", "'{'", "'id'", "'}'", "'attribute'", "'branchUuid'", "'attributeType'", "'overrides'", "'dataProvider'", "'DefaultAttributeDataProvider'", "'UriAttributeDataProvider'", "'min'", "'max'", "'unlimited'", "'taggerId'", "'DefaultAttributeTaggerProvider'", "'enumType'", "'description'", "'defaultValue'", "'fileExtension'", "'mediaType'", "'BooleanAttribute'", "'CompressedContentAttribute'", "'DateAttribute'", "'EnumeratedAttribute'", "'FloatingPointAttribute'", "'IntegerAttribute'", "'LongAttribute'", "'JavaObjectAttribute'", "'StringAttribute'", "'ArtifactReferenceAttribute'", "'BranchReferenceAttribute'", "'WordAttribute'", "'OutlineNumberAttribute'", "'oseeEnumType'", "'entry'", "'entryGuid'", "'overrides enum'", "'inheritAll'", "'add'", "'remove'", "'overrides artifactType'", "'update'", "'relationType'", "'sideAName'", "'sideAArtifactType'", "'sideBName'", "'sideBArtifactType'", "'defaultOrderType'", "'multiplicity'", "'Lexicographical_Ascending'", "'Lexicographical_Descending'", "'Unordered'", "'('", "')'", "'artifactMatcher'", "'where'", "';'", "'role'", "'accessContext'", "'guid'", "'childrenOf'", "'artifact'", "'edit'", "'of'", "'ONE_TO_ONE'", "'ONE_TO_MANY'", "'MANY_TO_ONE'", "'MANY_TO_MANY'", "'EQ'", "'LIKE'", "'AND'", "'OR'", "'artifactName'", "'artifactId'", "'branchName'", "'ALLOW'", "'DENY'", "'ALL'", "'SIDE_A'", "'SIDE_B'", "'BOTH'"
    };
    public static final int T__50=50;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=5;
    public static final int RULE_INT=7;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=8;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int RULE_WHOLE_NUM_STR=6;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__90=90;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__98=98;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int RULE_STRING=4;
    public static final int RULE_SL_COMMENT=9;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int RULE_WS=10;
    public static final int RULE_ANY_OTHER=11;
    public static final int T__88=88;
    public static final int T__89=89;
    public static final int T__84=84;
    public static final int T__85=85;
    public static final int T__86=86;
    public static final int T__87=87;

    // delegates
    // delegators


        public InternalOseeDslParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalOseeDslParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalOseeDslParser.tokenNames; }
    public String getGrammarFileName() { return "InternalOseeDsl.g"; }



     	private OseeDslGrammarAccess grammarAccess;
     	
        public InternalOseeDslParser(TokenStream input, OseeDslGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "OseeDsl";	
       	}
       	
       	@Override
       	protected OseeDslGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start "entryRuleOseeDsl"
    // InternalOseeDsl.g:68:1: entryRuleOseeDsl returns [EObject current=null] : iv_ruleOseeDsl= ruleOseeDsl EOF ;
    public final EObject entryRuleOseeDsl() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOseeDsl = null;


        try {
            // InternalOseeDsl.g:69:2: (iv_ruleOseeDsl= ruleOseeDsl EOF )
            // InternalOseeDsl.g:70:2: iv_ruleOseeDsl= ruleOseeDsl EOF
            {
             newCompositeNode(grammarAccess.getOseeDslRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOseeDsl=ruleOseeDsl();

            state._fsp--;

             current =iv_ruleOseeDsl; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOseeDsl"


    // $ANTLR start "ruleOseeDsl"
    // InternalOseeDsl.g:77:1: ruleOseeDsl returns [EObject current=null] : ( ( (lv_imports_0_0= ruleImport ) )* ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )* ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )* ) ;
    public final EObject ruleOseeDsl() throws RecognitionException {
        EObject current = null;

        EObject lv_imports_0_0 = null;

        EObject lv_artifactTypes_1_0 = null;

        EObject lv_relationTypes_2_0 = null;

        EObject lv_attributeTypes_3_0 = null;

        EObject lv_enumTypes_4_0 = null;

        EObject lv_enumOverrides_5_0 = null;

        EObject lv_artifactTypeOverrides_6_0 = null;

        EObject lv_artifactMatchRefs_7_0 = null;

        EObject lv_accessDeclarations_8_0 = null;

        EObject lv_roleDeclarations_9_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:80:28: ( ( ( (lv_imports_0_0= ruleImport ) )* ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )* ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )* ) )
            // InternalOseeDsl.g:81:1: ( ( (lv_imports_0_0= ruleImport ) )* ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )* ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )* )
            {
            // InternalOseeDsl.g:81:1: ( ( (lv_imports_0_0= ruleImport ) )* ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )* ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )* )
            // InternalOseeDsl.g:81:2: ( (lv_imports_0_0= ruleImport ) )* ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )* ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )*
            {
            // InternalOseeDsl.g:81:2: ( (lv_imports_0_0= ruleImport ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==12) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalOseeDsl.g:82:1: (lv_imports_0_0= ruleImport )
            	    {
            	    // InternalOseeDsl.g:82:1: (lv_imports_0_0= ruleImport )
            	    // InternalOseeDsl.g:83:3: lv_imports_0_0= ruleImport
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getImportsImportParserRuleCall_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_3);
            	    lv_imports_0_0=ruleImport();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"imports",
            	            		lv_imports_0_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.Import");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // InternalOseeDsl.g:99:3: ( ( (lv_artifactTypes_1_0= ruleXArtifactType ) ) | ( (lv_relationTypes_2_0= ruleXRelationType ) ) | ( (lv_attributeTypes_3_0= ruleXAttributeType ) ) | ( (lv_enumTypes_4_0= ruleXOseeEnumType ) ) | ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) ) | ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) ) )*
            loop2:
            do {
                int alt2=7;
                switch ( input.LA(1) ) {
                case 14:
                case 15:
                    {
                    alt2=1;
                    }
                    break;
                case 60:
                    {
                    alt2=2;
                    }
                    break;
                case 23:
                    {
                    alt2=3;
                    }
                    break;
                case 51:
                    {
                    alt2=4;
                    }
                    break;
                case 54:
                    {
                    alt2=5;
                    }
                    break;
                case 58:
                    {
                    alt2=6;
                    }
                    break;

                }

                switch (alt2) {
            	case 1 :
            	    // InternalOseeDsl.g:99:4: ( (lv_artifactTypes_1_0= ruleXArtifactType ) )
            	    {
            	    // InternalOseeDsl.g:99:4: ( (lv_artifactTypes_1_0= ruleXArtifactType ) )
            	    // InternalOseeDsl.g:100:1: (lv_artifactTypes_1_0= ruleXArtifactType )
            	    {
            	    // InternalOseeDsl.g:100:1: (lv_artifactTypes_1_0= ruleXArtifactType )
            	    // InternalOseeDsl.g:101:3: lv_artifactTypes_1_0= ruleXArtifactType
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getArtifactTypesXArtifactTypeParserRuleCall_1_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_artifactTypes_1_0=ruleXArtifactType();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"artifactTypes",
            	            		lv_artifactTypes_1_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XArtifactType");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // InternalOseeDsl.g:118:6: ( (lv_relationTypes_2_0= ruleXRelationType ) )
            	    {
            	    // InternalOseeDsl.g:118:6: ( (lv_relationTypes_2_0= ruleXRelationType ) )
            	    // InternalOseeDsl.g:119:1: (lv_relationTypes_2_0= ruleXRelationType )
            	    {
            	    // InternalOseeDsl.g:119:1: (lv_relationTypes_2_0= ruleXRelationType )
            	    // InternalOseeDsl.g:120:3: lv_relationTypes_2_0= ruleXRelationType
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getRelationTypesXRelationTypeParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_relationTypes_2_0=ruleXRelationType();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"relationTypes",
            	            		lv_relationTypes_2_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XRelationType");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 3 :
            	    // InternalOseeDsl.g:137:6: ( (lv_attributeTypes_3_0= ruleXAttributeType ) )
            	    {
            	    // InternalOseeDsl.g:137:6: ( (lv_attributeTypes_3_0= ruleXAttributeType ) )
            	    // InternalOseeDsl.g:138:1: (lv_attributeTypes_3_0= ruleXAttributeType )
            	    {
            	    // InternalOseeDsl.g:138:1: (lv_attributeTypes_3_0= ruleXAttributeType )
            	    // InternalOseeDsl.g:139:3: lv_attributeTypes_3_0= ruleXAttributeType
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getAttributeTypesXAttributeTypeParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_attributeTypes_3_0=ruleXAttributeType();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"attributeTypes",
            	            		lv_attributeTypes_3_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XAttributeType");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 4 :
            	    // InternalOseeDsl.g:156:6: ( (lv_enumTypes_4_0= ruleXOseeEnumType ) )
            	    {
            	    // InternalOseeDsl.g:156:6: ( (lv_enumTypes_4_0= ruleXOseeEnumType ) )
            	    // InternalOseeDsl.g:157:1: (lv_enumTypes_4_0= ruleXOseeEnumType )
            	    {
            	    // InternalOseeDsl.g:157:1: (lv_enumTypes_4_0= ruleXOseeEnumType )
            	    // InternalOseeDsl.g:158:3: lv_enumTypes_4_0= ruleXOseeEnumType
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getEnumTypesXOseeEnumTypeParserRuleCall_1_3_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_enumTypes_4_0=ruleXOseeEnumType();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"enumTypes",
            	            		lv_enumTypes_4_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XOseeEnumType");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 5 :
            	    // InternalOseeDsl.g:175:6: ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) )
            	    {
            	    // InternalOseeDsl.g:175:6: ( (lv_enumOverrides_5_0= ruleXOseeEnumOverride ) )
            	    // InternalOseeDsl.g:176:1: (lv_enumOverrides_5_0= ruleXOseeEnumOverride )
            	    {
            	    // InternalOseeDsl.g:176:1: (lv_enumOverrides_5_0= ruleXOseeEnumOverride )
            	    // InternalOseeDsl.g:177:3: lv_enumOverrides_5_0= ruleXOseeEnumOverride
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getEnumOverridesXOseeEnumOverrideParserRuleCall_1_4_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_enumOverrides_5_0=ruleXOseeEnumOverride();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"enumOverrides",
            	            		lv_enumOverrides_5_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XOseeEnumOverride");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 6 :
            	    // InternalOseeDsl.g:194:6: ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) )
            	    {
            	    // InternalOseeDsl.g:194:6: ( (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride ) )
            	    // InternalOseeDsl.g:195:1: (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride )
            	    {
            	    // InternalOseeDsl.g:195:1: (lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride )
            	    // InternalOseeDsl.g:196:3: lv_artifactTypeOverrides_6_0= ruleXOseeArtifactTypeOverride
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getArtifactTypeOverridesXOseeArtifactTypeOverrideParserRuleCall_1_5_0()); 
            	    	    
            	    pushFollow(FOLLOW_4);
            	    lv_artifactTypeOverrides_6_0=ruleXOseeArtifactTypeOverride();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"artifactTypeOverrides",
            	            		lv_artifactTypeOverrides_6_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XOseeArtifactTypeOverride");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // InternalOseeDsl.g:212:4: ( ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) ) | ( (lv_accessDeclarations_8_0= ruleAccessContext ) ) | ( (lv_roleDeclarations_9_0= ruleRole ) ) )*
            loop3:
            do {
                int alt3=4;
                switch ( input.LA(1) ) {
                case 72:
                    {
                    alt3=1;
                    }
                    break;
                case 76:
                    {
                    alt3=2;
                    }
                    break;
                case 75:
                    {
                    alt3=3;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // InternalOseeDsl.g:212:5: ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) )
            	    {
            	    // InternalOseeDsl.g:212:5: ( (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher ) )
            	    // InternalOseeDsl.g:213:1: (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher )
            	    {
            	    // InternalOseeDsl.g:213:1: (lv_artifactMatchRefs_7_0= ruleXArtifactMatcher )
            	    // InternalOseeDsl.g:214:3: lv_artifactMatchRefs_7_0= ruleXArtifactMatcher
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getArtifactMatchRefsXArtifactMatcherParserRuleCall_2_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_5);
            	    lv_artifactMatchRefs_7_0=ruleXArtifactMatcher();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"artifactMatchRefs",
            	            		lv_artifactMatchRefs_7_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XArtifactMatcher");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // InternalOseeDsl.g:231:6: ( (lv_accessDeclarations_8_0= ruleAccessContext ) )
            	    {
            	    // InternalOseeDsl.g:231:6: ( (lv_accessDeclarations_8_0= ruleAccessContext ) )
            	    // InternalOseeDsl.g:232:1: (lv_accessDeclarations_8_0= ruleAccessContext )
            	    {
            	    // InternalOseeDsl.g:232:1: (lv_accessDeclarations_8_0= ruleAccessContext )
            	    // InternalOseeDsl.g:233:3: lv_accessDeclarations_8_0= ruleAccessContext
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getAccessDeclarationsAccessContextParserRuleCall_2_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_5);
            	    lv_accessDeclarations_8_0=ruleAccessContext();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"accessDeclarations",
            	            		lv_accessDeclarations_8_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.AccessContext");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 3 :
            	    // InternalOseeDsl.g:250:6: ( (lv_roleDeclarations_9_0= ruleRole ) )
            	    {
            	    // InternalOseeDsl.g:250:6: ( (lv_roleDeclarations_9_0= ruleRole ) )
            	    // InternalOseeDsl.g:251:1: (lv_roleDeclarations_9_0= ruleRole )
            	    {
            	    // InternalOseeDsl.g:251:1: (lv_roleDeclarations_9_0= ruleRole )
            	    // InternalOseeDsl.g:252:3: lv_roleDeclarations_9_0= ruleRole
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOseeDslAccess().getRoleDeclarationsRoleParserRuleCall_2_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_5);
            	    lv_roleDeclarations_9_0=ruleRole();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOseeDslRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"roleDeclarations",
            	            		lv_roleDeclarations_9_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.Role");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOseeDsl"


    // $ANTLR start "entryRuleImport"
    // InternalOseeDsl.g:276:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // InternalOseeDsl.g:277:2: (iv_ruleImport= ruleImport EOF )
            // InternalOseeDsl.g:278:2: iv_ruleImport= ruleImport EOF
            {
             newCompositeNode(grammarAccess.getImportRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleImport=ruleImport();

            state._fsp--;

             current =iv_ruleImport; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleImport"


    // $ANTLR start "ruleImport"
    // InternalOseeDsl.g:285:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_importURI_1_0=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:288:28: ( (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:289:1: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:289:1: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) )
            // InternalOseeDsl.g:289:3: otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,12,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
                
            // InternalOseeDsl.g:293:1: ( (lv_importURI_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:294:1: (lv_importURI_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:294:1: (lv_importURI_1_0= RULE_STRING )
            // InternalOseeDsl.g:295:3: lv_importURI_1_0= RULE_STRING
            {
            lv_importURI_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            			newLeafNode(lv_importURI_1_0, grammarAccess.getImportAccess().getImportURISTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getImportRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"importURI",
                    		lv_importURI_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleImport"


    // $ANTLR start "entryRuleQUALIFIED_NAME"
    // InternalOseeDsl.g:319:1: entryRuleQUALIFIED_NAME returns [String current=null] : iv_ruleQUALIFIED_NAME= ruleQUALIFIED_NAME EOF ;
    public final String entryRuleQUALIFIED_NAME() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleQUALIFIED_NAME = null;


        try {
            // InternalOseeDsl.g:320:2: (iv_ruleQUALIFIED_NAME= ruleQUALIFIED_NAME EOF )
            // InternalOseeDsl.g:321:2: iv_ruleQUALIFIED_NAME= ruleQUALIFIED_NAME EOF
            {
             newCompositeNode(grammarAccess.getQUALIFIED_NAMERule()); 
            pushFollow(FOLLOW_1);
            iv_ruleQUALIFIED_NAME=ruleQUALIFIED_NAME();

            state._fsp--;

             current =iv_ruleQUALIFIED_NAME.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleQUALIFIED_NAME"


    // $ANTLR start "ruleQUALIFIED_NAME"
    // InternalOseeDsl.g:328:1: ruleQUALIFIED_NAME returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* ) ;
    public final AntlrDatatypeRuleToken ruleQUALIFIED_NAME() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;
        Token this_ID_2=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:331:28: ( (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* ) )
            // InternalOseeDsl.g:332:1: (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* )
            {
            // InternalOseeDsl.g:332:1: (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* )
            // InternalOseeDsl.g:332:6: this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )*
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_7); 

            		current.merge(this_ID_0);
                
             
                newLeafNode(this_ID_0, grammarAccess.getQUALIFIED_NAMEAccess().getIDTerminalRuleCall_0()); 
                
            // InternalOseeDsl.g:339:1: (kw= '.' this_ID_2= RULE_ID )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==13) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalOseeDsl.g:340:2: kw= '.' this_ID_2= RULE_ID
            	    {
            	    kw=(Token)match(input,13,FOLLOW_8); 

            	            current.merge(kw);
            	            newLeafNode(kw, grammarAccess.getQUALIFIED_NAMEAccess().getFullStopKeyword_1_0()); 
            	        
            	    this_ID_2=(Token)match(input,RULE_ID,FOLLOW_7); 

            	    		current.merge(this_ID_2);
            	        
            	     
            	        newLeafNode(this_ID_2, grammarAccess.getQUALIFIED_NAMEAccess().getIDTerminalRuleCall_1_1()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleQUALIFIED_NAME"


    // $ANTLR start "entryRuleOseeType"
    // InternalOseeDsl.g:362:1: entryRuleOseeType returns [EObject current=null] : iv_ruleOseeType= ruleOseeType EOF ;
    public final EObject entryRuleOseeType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOseeType = null;


        try {
            // InternalOseeDsl.g:363:2: (iv_ruleOseeType= ruleOseeType EOF )
            // InternalOseeDsl.g:364:2: iv_ruleOseeType= ruleOseeType EOF
            {
             newCompositeNode(grammarAccess.getOseeTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOseeType=ruleOseeType();

            state._fsp--;

             current =iv_ruleOseeType; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOseeType"


    // $ANTLR start "ruleOseeType"
    // InternalOseeDsl.g:371:1: ruleOseeType returns [EObject current=null] : (this_XArtifactType_0= ruleXArtifactType | this_XRelationType_1= ruleXRelationType | this_XAttributeType_2= ruleXAttributeType | this_XOseeEnumType_3= ruleXOseeEnumType ) ;
    public final EObject ruleOseeType() throws RecognitionException {
        EObject current = null;

        EObject this_XArtifactType_0 = null;

        EObject this_XRelationType_1 = null;

        EObject this_XAttributeType_2 = null;

        EObject this_XOseeEnumType_3 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:374:28: ( (this_XArtifactType_0= ruleXArtifactType | this_XRelationType_1= ruleXRelationType | this_XAttributeType_2= ruleXAttributeType | this_XOseeEnumType_3= ruleXOseeEnumType ) )
            // InternalOseeDsl.g:375:1: (this_XArtifactType_0= ruleXArtifactType | this_XRelationType_1= ruleXRelationType | this_XAttributeType_2= ruleXAttributeType | this_XOseeEnumType_3= ruleXOseeEnumType )
            {
            // InternalOseeDsl.g:375:1: (this_XArtifactType_0= ruleXArtifactType | this_XRelationType_1= ruleXRelationType | this_XAttributeType_2= ruleXAttributeType | this_XOseeEnumType_3= ruleXOseeEnumType )
            int alt5=4;
            switch ( input.LA(1) ) {
            case 14:
            case 15:
                {
                alt5=1;
                }
                break;
            case 60:
                {
                alt5=2;
                }
                break;
            case 23:
                {
                alt5=3;
                }
                break;
            case 51:
                {
                alt5=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // InternalOseeDsl.g:376:5: this_XArtifactType_0= ruleXArtifactType
                    {
                     
                            newCompositeNode(grammarAccess.getOseeTypeAccess().getXArtifactTypeParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_XArtifactType_0=ruleXArtifactType();

                    state._fsp--;

                     
                            current = this_XArtifactType_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:386:5: this_XRelationType_1= ruleXRelationType
                    {
                     
                            newCompositeNode(grammarAccess.getOseeTypeAccess().getXRelationTypeParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_XRelationType_1=ruleXRelationType();

                    state._fsp--;

                     
                            current = this_XRelationType_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:396:5: this_XAttributeType_2= ruleXAttributeType
                    {
                     
                            newCompositeNode(grammarAccess.getOseeTypeAccess().getXAttributeTypeParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_2);
                    this_XAttributeType_2=ruleXAttributeType();

                    state._fsp--;

                     
                            current = this_XAttributeType_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:406:5: this_XOseeEnumType_3= ruleXOseeEnumType
                    {
                     
                            newCompositeNode(grammarAccess.getOseeTypeAccess().getXOseeEnumTypeParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_2);
                    this_XOseeEnumType_3=ruleXOseeEnumType();

                    state._fsp--;

                     
                            current = this_XOseeEnumType_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOseeType"


    // $ANTLR start "entryRuleXArtifactType"
    // InternalOseeDsl.g:422:1: entryRuleXArtifactType returns [EObject current=null] : iv_ruleXArtifactType= ruleXArtifactType EOF ;
    public final EObject entryRuleXArtifactType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXArtifactType = null;


        try {
            // InternalOseeDsl.g:423:2: (iv_ruleXArtifactType= ruleXArtifactType EOF )
            // InternalOseeDsl.g:424:2: iv_ruleXArtifactType= ruleXArtifactType EOF
            {
             newCompositeNode(grammarAccess.getXArtifactTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXArtifactType=ruleXArtifactType();

            state._fsp--;

             current =iv_ruleXArtifactType; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXArtifactType"


    // $ANTLR start "ruleXArtifactType"
    // InternalOseeDsl.g:431:1: ruleXArtifactType returns [EObject current=null] : ( ( (lv_abstract_0_0= 'abstract' ) )? otherlv_1= 'artifactType' ( (lv_name_2_0= RULE_STRING ) ) (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )? otherlv_7= '{' otherlv_8= 'id' ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) ) ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )* otherlv_11= '}' ) ;
    public final EObject ruleXArtifactType() throws RecognitionException {
        EObject current = null;

        Token lv_abstract_0_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token lv_id_9_0=null;
        Token otherlv_11=null;
        EObject lv_validAttributeTypes_10_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:434:28: ( ( ( (lv_abstract_0_0= 'abstract' ) )? otherlv_1= 'artifactType' ( (lv_name_2_0= RULE_STRING ) ) (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )? otherlv_7= '{' otherlv_8= 'id' ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) ) ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )* otherlv_11= '}' ) )
            // InternalOseeDsl.g:435:1: ( ( (lv_abstract_0_0= 'abstract' ) )? otherlv_1= 'artifactType' ( (lv_name_2_0= RULE_STRING ) ) (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )? otherlv_7= '{' otherlv_8= 'id' ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) ) ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )* otherlv_11= '}' )
            {
            // InternalOseeDsl.g:435:1: ( ( (lv_abstract_0_0= 'abstract' ) )? otherlv_1= 'artifactType' ( (lv_name_2_0= RULE_STRING ) ) (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )? otherlv_7= '{' otherlv_8= 'id' ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) ) ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )* otherlv_11= '}' )
            // InternalOseeDsl.g:435:2: ( (lv_abstract_0_0= 'abstract' ) )? otherlv_1= 'artifactType' ( (lv_name_2_0= RULE_STRING ) ) (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )? otherlv_7= '{' otherlv_8= 'id' ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) ) ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )* otherlv_11= '}'
            {
            // InternalOseeDsl.g:435:2: ( (lv_abstract_0_0= 'abstract' ) )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==14) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // InternalOseeDsl.g:436:1: (lv_abstract_0_0= 'abstract' )
                    {
                    // InternalOseeDsl.g:436:1: (lv_abstract_0_0= 'abstract' )
                    // InternalOseeDsl.g:437:3: lv_abstract_0_0= 'abstract'
                    {
                    lv_abstract_0_0=(Token)match(input,14,FOLLOW_9); 

                            newLeafNode(lv_abstract_0_0, grammarAccess.getXArtifactTypeAccess().getAbstractAbstractKeyword_0_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXArtifactTypeRule());
                    	        }
                           		setWithLastConsumed(current, "abstract", true, "abstract");
                    	    

                    }


                    }
                    break;

            }

            otherlv_1=(Token)match(input,15,FOLLOW_6); 

                	newLeafNode(otherlv_1, grammarAccess.getXArtifactTypeAccess().getArtifactTypeKeyword_1());
                
            // InternalOseeDsl.g:454:1: ( (lv_name_2_0= RULE_STRING ) )
            // InternalOseeDsl.g:455:1: (lv_name_2_0= RULE_STRING )
            {
            // InternalOseeDsl.g:455:1: (lv_name_2_0= RULE_STRING )
            // InternalOseeDsl.g:456:3: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_10); 

            			newLeafNode(lv_name_2_0, grammarAccess.getXArtifactTypeAccess().getNameSTRINGTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXArtifactTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:472:2: (otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )* )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==16) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // InternalOseeDsl.g:472:4: otherlv_3= 'extends' ( (otherlv_4= RULE_STRING ) ) (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )*
                    {
                    otherlv_3=(Token)match(input,16,FOLLOW_6); 

                        	newLeafNode(otherlv_3, grammarAccess.getXArtifactTypeAccess().getExtendsKeyword_3_0());
                        
                    // InternalOseeDsl.g:476:1: ( (otherlv_4= RULE_STRING ) )
                    // InternalOseeDsl.g:477:1: (otherlv_4= RULE_STRING )
                    {
                    // InternalOseeDsl.g:477:1: (otherlv_4= RULE_STRING )
                    // InternalOseeDsl.g:478:3: otherlv_4= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getXArtifactTypeRule());
                    	        }
                            
                    otherlv_4=(Token)match(input,RULE_STRING,FOLLOW_11); 

                    		newLeafNode(otherlv_4, grammarAccess.getXArtifactTypeAccess().getSuperArtifactTypesXArtifactTypeCrossReference_3_1_0()); 
                    	

                    }


                    }

                    // InternalOseeDsl.g:489:2: (otherlv_5= ',' ( (otherlv_6= RULE_STRING ) ) )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==17) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // InternalOseeDsl.g:489:4: otherlv_5= ',' ( (otherlv_6= RULE_STRING ) )
                    	    {
                    	    otherlv_5=(Token)match(input,17,FOLLOW_6); 

                    	        	newLeafNode(otherlv_5, grammarAccess.getXArtifactTypeAccess().getCommaKeyword_3_2_0());
                    	        
                    	    // InternalOseeDsl.g:493:1: ( (otherlv_6= RULE_STRING ) )
                    	    // InternalOseeDsl.g:494:1: (otherlv_6= RULE_STRING )
                    	    {
                    	    // InternalOseeDsl.g:494:1: (otherlv_6= RULE_STRING )
                    	    // InternalOseeDsl.g:495:3: otherlv_6= RULE_STRING
                    	    {

                    	    			if (current==null) {
                    	    	            current = createModelElement(grammarAccess.getXArtifactTypeRule());
                    	    	        }
                    	            
                    	    otherlv_6=(Token)match(input,RULE_STRING,FOLLOW_11); 

                    	    		newLeafNode(otherlv_6, grammarAccess.getXArtifactTypeAccess().getSuperArtifactTypesXArtifactTypeCrossReference_3_2_1_0()); 
                    	    	

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);


                    }
                    break;

            }

            otherlv_7=(Token)match(input,18,FOLLOW_12); 

                	newLeafNode(otherlv_7, grammarAccess.getXArtifactTypeAccess().getLeftCurlyBracketKeyword_4());
                
            otherlv_8=(Token)match(input,19,FOLLOW_13); 

                	newLeafNode(otherlv_8, grammarAccess.getXArtifactTypeAccess().getIdKeyword_5());
                
            // InternalOseeDsl.g:514:1: ( (lv_id_9_0= RULE_WHOLE_NUM_STR ) )
            // InternalOseeDsl.g:515:1: (lv_id_9_0= RULE_WHOLE_NUM_STR )
            {
            // InternalOseeDsl.g:515:1: (lv_id_9_0= RULE_WHOLE_NUM_STR )
            // InternalOseeDsl.g:516:3: lv_id_9_0= RULE_WHOLE_NUM_STR
            {
            lv_id_9_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_14); 

            			newLeafNode(lv_id_9_0, grammarAccess.getXArtifactTypeAccess().getIdWHOLE_NUM_STRTerminalRuleCall_6_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXArtifactTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"id",
                    		lv_id_9_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
            	    

            }


            }

            // InternalOseeDsl.g:532:2: ( (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==21) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // InternalOseeDsl.g:533:1: (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef )
            	    {
            	    // InternalOseeDsl.g:533:1: (lv_validAttributeTypes_10_0= ruleXAttributeTypeRef )
            	    // InternalOseeDsl.g:534:3: lv_validAttributeTypes_10_0= ruleXAttributeTypeRef
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXArtifactTypeAccess().getValidAttributeTypesXAttributeTypeRefParserRuleCall_7_0()); 
            	    	    
            	    pushFollow(FOLLOW_14);
            	    lv_validAttributeTypes_10_0=ruleXAttributeTypeRef();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXArtifactTypeRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"validAttributeTypes",
            	            		lv_validAttributeTypes_10_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XAttributeTypeRef");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            otherlv_11=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_11, grammarAccess.getXArtifactTypeAccess().getRightCurlyBracketKeyword_8());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXArtifactType"


    // $ANTLR start "entryRuleXAttributeTypeRef"
    // InternalOseeDsl.g:562:1: entryRuleXAttributeTypeRef returns [EObject current=null] : iv_ruleXAttributeTypeRef= ruleXAttributeTypeRef EOF ;
    public final EObject entryRuleXAttributeTypeRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXAttributeTypeRef = null;


        try {
            // InternalOseeDsl.g:563:2: (iv_ruleXAttributeTypeRef= ruleXAttributeTypeRef EOF )
            // InternalOseeDsl.g:564:2: iv_ruleXAttributeTypeRef= ruleXAttributeTypeRef EOF
            {
             newCompositeNode(grammarAccess.getXAttributeTypeRefRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXAttributeTypeRef=ruleXAttributeTypeRef();

            state._fsp--;

             current =iv_ruleXAttributeTypeRef; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXAttributeTypeRef"


    // $ANTLR start "ruleXAttributeTypeRef"
    // InternalOseeDsl.g:571:1: ruleXAttributeTypeRef returns [EObject current=null] : (otherlv_0= 'attribute' ( (otherlv_1= RULE_STRING ) ) (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )? ) ;
    public final EObject ruleXAttributeTypeRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;
        Token lv_branchUuid_3_0=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:574:28: ( (otherlv_0= 'attribute' ( (otherlv_1= RULE_STRING ) ) (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )? ) )
            // InternalOseeDsl.g:575:1: (otherlv_0= 'attribute' ( (otherlv_1= RULE_STRING ) ) (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )? )
            {
            // InternalOseeDsl.g:575:1: (otherlv_0= 'attribute' ( (otherlv_1= RULE_STRING ) ) (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )? )
            // InternalOseeDsl.g:575:3: otherlv_0= 'attribute' ( (otherlv_1= RULE_STRING ) ) (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )?
            {
            otherlv_0=(Token)match(input,21,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXAttributeTypeRefAccess().getAttributeKeyword_0());
                
            // InternalOseeDsl.g:579:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:580:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:580:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:581:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getXAttributeTypeRefRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_15); 

            		newLeafNode(otherlv_1, grammarAccess.getXAttributeTypeRefAccess().getValidAttributeTypeXAttributeTypeCrossReference_1_0()); 
            	

            }


            }

            // InternalOseeDsl.g:592:2: (otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) ) )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==22) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // InternalOseeDsl.g:592:4: otherlv_2= 'branchUuid' ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) )
                    {
                    otherlv_2=(Token)match(input,22,FOLLOW_13); 

                        	newLeafNode(otherlv_2, grammarAccess.getXAttributeTypeRefAccess().getBranchUuidKeyword_2_0());
                        
                    // InternalOseeDsl.g:596:1: ( (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR ) )
                    // InternalOseeDsl.g:597:1: (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR )
                    {
                    // InternalOseeDsl.g:597:1: (lv_branchUuid_3_0= RULE_WHOLE_NUM_STR )
                    // InternalOseeDsl.g:598:3: lv_branchUuid_3_0= RULE_WHOLE_NUM_STR
                    {
                    lv_branchUuid_3_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_2); 

                    			newLeafNode(lv_branchUuid_3_0, grammarAccess.getXAttributeTypeRefAccess().getBranchUuidWHOLE_NUM_STRTerminalRuleCall_2_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"branchUuid",
                            		lv_branchUuid_3_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
                    	    

                    }


                    }


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXAttributeTypeRef"


    // $ANTLR start "entryRuleXAttributeType"
    // InternalOseeDsl.g:622:1: entryRuleXAttributeType returns [EObject current=null] : iv_ruleXAttributeType= ruleXAttributeType EOF ;
    public final EObject entryRuleXAttributeType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXAttributeType = null;


        try {
            // InternalOseeDsl.g:623:2: (iv_ruleXAttributeType= ruleXAttributeType EOF )
            // InternalOseeDsl.g:624:2: iv_ruleXAttributeType= ruleXAttributeType EOF
            {
             newCompositeNode(grammarAccess.getXAttributeTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXAttributeType=ruleXAttributeType();

            state._fsp--;

             current =iv_ruleXAttributeType; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXAttributeType"


    // $ANTLR start "ruleXAttributeType"
    // InternalOseeDsl.g:631:1: ruleXAttributeType returns [EObject current=null] : (otherlv_0= 'attributeType' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) ) (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )? otherlv_6= '{' otherlv_7= 'id' ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) ) otherlv_9= 'dataProvider' ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) ) otherlv_11= 'min' ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) ) otherlv_13= 'max' ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) ) ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) ) otherlv_28= '}' ) ;
    public final EObject ruleXAttributeType() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token lv_id_8_0=null;
        Token otherlv_9=null;
        Token lv_dataProvider_10_1=null;
        Token lv_dataProvider_10_2=null;
        Token otherlv_11=null;
        Token lv_min_12_0=null;
        Token otherlv_13=null;
        Token lv_max_14_1=null;
        Token lv_max_14_2=null;
        Token otherlv_16=null;
        Token lv_taggerId_17_1=null;
        Token otherlv_18=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token lv_description_21_0=null;
        Token otherlv_22=null;
        Token lv_defaultValue_23_0=null;
        Token otherlv_24=null;
        Token lv_fileExtension_25_0=null;
        Token otherlv_26=null;
        Token lv_mediaType_27_0=null;
        Token otherlv_28=null;
        AntlrDatatypeRuleToken lv_baseAttributeType_3_0 = null;

        AntlrDatatypeRuleToken lv_dataProvider_10_3 = null;

        AntlrDatatypeRuleToken lv_taggerId_17_2 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:634:28: ( (otherlv_0= 'attributeType' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) ) (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )? otherlv_6= '{' otherlv_7= 'id' ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) ) otherlv_9= 'dataProvider' ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) ) otherlv_11= 'min' ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) ) otherlv_13= 'max' ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) ) ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) ) otherlv_28= '}' ) )
            // InternalOseeDsl.g:635:1: (otherlv_0= 'attributeType' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) ) (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )? otherlv_6= '{' otherlv_7= 'id' ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) ) otherlv_9= 'dataProvider' ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) ) otherlv_11= 'min' ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) ) otherlv_13= 'max' ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) ) ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) ) otherlv_28= '}' )
            {
            // InternalOseeDsl.g:635:1: (otherlv_0= 'attributeType' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) ) (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )? otherlv_6= '{' otherlv_7= 'id' ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) ) otherlv_9= 'dataProvider' ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) ) otherlv_11= 'min' ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) ) otherlv_13= 'max' ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) ) ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) ) otherlv_28= '}' )
            // InternalOseeDsl.g:635:3: otherlv_0= 'attributeType' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) ) (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )? otherlv_6= '{' otherlv_7= 'id' ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) ) otherlv_9= 'dataProvider' ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) ) otherlv_11= 'min' ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) ) otherlv_13= 'max' ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) ) ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) ) otherlv_28= '}'
            {
            otherlv_0=(Token)match(input,23,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXAttributeTypeAccess().getAttributeTypeKeyword_0());
                
            // InternalOseeDsl.g:639:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:640:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:640:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:641:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            			newLeafNode(lv_name_1_0, grammarAccess.getXAttributeTypeAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:657:2: (otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) ) )
            // InternalOseeDsl.g:657:4: otherlv_2= 'extends' ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) )
            {
            otherlv_2=(Token)match(input,16,FOLLOW_17); 

                	newLeafNode(otherlv_2, grammarAccess.getXAttributeTypeAccess().getExtendsKeyword_2_0());
                
            // InternalOseeDsl.g:661:1: ( (lv_baseAttributeType_3_0= ruleAttributeBaseType ) )
            // InternalOseeDsl.g:662:1: (lv_baseAttributeType_3_0= ruleAttributeBaseType )
            {
            // InternalOseeDsl.g:662:1: (lv_baseAttributeType_3_0= ruleAttributeBaseType )
            // InternalOseeDsl.g:663:3: lv_baseAttributeType_3_0= ruleAttributeBaseType
            {
             
            	        newCompositeNode(grammarAccess.getXAttributeTypeAccess().getBaseAttributeTypeAttributeBaseTypeParserRuleCall_2_1_0()); 
            	    
            pushFollow(FOLLOW_18);
            lv_baseAttributeType_3_0=ruleAttributeBaseType();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getXAttributeTypeRule());
            	        }
                   		set(
                   			current, 
                   			"baseAttributeType",
                    		lv_baseAttributeType_3_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.AttributeBaseType");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }

            // InternalOseeDsl.g:679:3: (otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) ) )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==24) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // InternalOseeDsl.g:679:5: otherlv_4= 'overrides' ( (otherlv_5= RULE_STRING ) )
                    {
                    otherlv_4=(Token)match(input,24,FOLLOW_6); 

                        	newLeafNode(otherlv_4, grammarAccess.getXAttributeTypeAccess().getOverridesKeyword_3_0());
                        
                    // InternalOseeDsl.g:683:1: ( (otherlv_5= RULE_STRING ) )
                    // InternalOseeDsl.g:684:1: (otherlv_5= RULE_STRING )
                    {
                    // InternalOseeDsl.g:684:1: (otherlv_5= RULE_STRING )
                    // InternalOseeDsl.g:685:3: otherlv_5= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
                    	        }
                            
                    otherlv_5=(Token)match(input,RULE_STRING,FOLLOW_19); 

                    		newLeafNode(otherlv_5, grammarAccess.getXAttributeTypeAccess().getOverrideXAttributeTypeCrossReference_3_1_0()); 
                    	

                    }


                    }


                    }
                    break;

            }

            otherlv_6=(Token)match(input,18,FOLLOW_12); 

                	newLeafNode(otherlv_6, grammarAccess.getXAttributeTypeAccess().getLeftCurlyBracketKeyword_4());
                
            otherlv_7=(Token)match(input,19,FOLLOW_13); 

                	newLeafNode(otherlv_7, grammarAccess.getXAttributeTypeAccess().getIdKeyword_5());
                
            // InternalOseeDsl.g:704:1: ( (lv_id_8_0= RULE_WHOLE_NUM_STR ) )
            // InternalOseeDsl.g:705:1: (lv_id_8_0= RULE_WHOLE_NUM_STR )
            {
            // InternalOseeDsl.g:705:1: (lv_id_8_0= RULE_WHOLE_NUM_STR )
            // InternalOseeDsl.g:706:3: lv_id_8_0= RULE_WHOLE_NUM_STR
            {
            lv_id_8_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_20); 

            			newLeafNode(lv_id_8_0, grammarAccess.getXAttributeTypeAccess().getIdWHOLE_NUM_STRTerminalRuleCall_6_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"id",
                    		lv_id_8_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
            	    

            }


            }

            otherlv_9=(Token)match(input,25,FOLLOW_21); 

                	newLeafNode(otherlv_9, grammarAccess.getXAttributeTypeAccess().getDataProviderKeyword_7());
                
            // InternalOseeDsl.g:726:1: ( ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) ) )
            // InternalOseeDsl.g:727:1: ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) )
            {
            // InternalOseeDsl.g:727:1: ( (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME ) )
            // InternalOseeDsl.g:728:1: (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME )
            {
            // InternalOseeDsl.g:728:1: (lv_dataProvider_10_1= 'DefaultAttributeDataProvider' | lv_dataProvider_10_2= 'UriAttributeDataProvider' | lv_dataProvider_10_3= ruleQUALIFIED_NAME )
            int alt12=3;
            switch ( input.LA(1) ) {
            case 26:
                {
                alt12=1;
                }
                break;
            case 27:
                {
                alt12=2;
                }
                break;
            case RULE_ID:
                {
                alt12=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // InternalOseeDsl.g:729:3: lv_dataProvider_10_1= 'DefaultAttributeDataProvider'
                    {
                    lv_dataProvider_10_1=(Token)match(input,26,FOLLOW_22); 

                            newLeafNode(lv_dataProvider_10_1, grammarAccess.getXAttributeTypeAccess().getDataProviderDefaultAttributeDataProviderKeyword_8_0_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
                    	        }
                           		setWithLastConsumed(current, "dataProvider", lv_dataProvider_10_1, null);
                    	    

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:741:8: lv_dataProvider_10_2= 'UriAttributeDataProvider'
                    {
                    lv_dataProvider_10_2=(Token)match(input,27,FOLLOW_22); 

                            newLeafNode(lv_dataProvider_10_2, grammarAccess.getXAttributeTypeAccess().getDataProviderUriAttributeDataProviderKeyword_8_0_1());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
                    	        }
                           		setWithLastConsumed(current, "dataProvider", lv_dataProvider_10_2, null);
                    	    

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:753:8: lv_dataProvider_10_3= ruleQUALIFIED_NAME
                    {
                     
                    	        newCompositeNode(grammarAccess.getXAttributeTypeAccess().getDataProviderQUALIFIED_NAMEParserRuleCall_8_0_2()); 
                    	    
                    pushFollow(FOLLOW_22);
                    lv_dataProvider_10_3=ruleQUALIFIED_NAME();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getXAttributeTypeRule());
                    	        }
                           		set(
                           			current, 
                           			"dataProvider",
                            		lv_dataProvider_10_3, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.QUALIFIED_NAME");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;

            }


            }


            }

            otherlv_11=(Token)match(input,28,FOLLOW_13); 

                	newLeafNode(otherlv_11, grammarAccess.getXAttributeTypeAccess().getMinKeyword_9());
                
            // InternalOseeDsl.g:775:1: ( (lv_min_12_0= RULE_WHOLE_NUM_STR ) )
            // InternalOseeDsl.g:776:1: (lv_min_12_0= RULE_WHOLE_NUM_STR )
            {
            // InternalOseeDsl.g:776:1: (lv_min_12_0= RULE_WHOLE_NUM_STR )
            // InternalOseeDsl.g:777:3: lv_min_12_0= RULE_WHOLE_NUM_STR
            {
            lv_min_12_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_23); 

            			newLeafNode(lv_min_12_0, grammarAccess.getXAttributeTypeAccess().getMinWHOLE_NUM_STRTerminalRuleCall_10_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"min",
                    		lv_min_12_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
            	    

            }


            }

            otherlv_13=(Token)match(input,29,FOLLOW_24); 

                	newLeafNode(otherlv_13, grammarAccess.getXAttributeTypeAccess().getMaxKeyword_11());
                
            // InternalOseeDsl.g:797:1: ( ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) ) )
            // InternalOseeDsl.g:798:1: ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) )
            {
            // InternalOseeDsl.g:798:1: ( (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' ) )
            // InternalOseeDsl.g:799:1: (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' )
            {
            // InternalOseeDsl.g:799:1: (lv_max_14_1= RULE_WHOLE_NUM_STR | lv_max_14_2= 'unlimited' )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_WHOLE_NUM_STR) ) {
                alt13=1;
            }
            else if ( (LA13_0==30) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // InternalOseeDsl.g:800:3: lv_max_14_1= RULE_WHOLE_NUM_STR
                    {
                    lv_max_14_1=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_25); 

                    			newLeafNode(lv_max_14_1, grammarAccess.getXAttributeTypeAccess().getMaxWHOLE_NUM_STRTerminalRuleCall_12_0_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"max",
                            		lv_max_14_1, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
                    	    

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:815:8: lv_max_14_2= 'unlimited'
                    {
                    lv_max_14_2=(Token)match(input,30,FOLLOW_25); 

                            newLeafNode(lv_max_14_2, grammarAccess.getXAttributeTypeAccess().getMaxUnlimitedKeyword_12_0_1());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
                    	        }
                           		setWithLastConsumed(current, "max", lv_max_14_2, null);
                    	    

                    }
                    break;

            }


            }


            }

            // InternalOseeDsl.g:830:2: ( ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) ) )
            // InternalOseeDsl.g:832:1: ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) )
            {
            // InternalOseeDsl.g:832:1: ( ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* ) )
            // InternalOseeDsl.g:833:2: ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* )
            {
             
            	  getUnorderedGroupHelper().enter(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	
            // InternalOseeDsl.g:836:2: ( ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )* )
            // InternalOseeDsl.g:837:3: ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )*
            {
            // InternalOseeDsl.g:837:3: ( ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) ) | ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) ) )*
            loop15:
            do {
                int alt15=7;
                int LA15_0 = input.LA(1);

                if ( LA15_0 == 31 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 0) ) {
                    alt15=1;
                }
                else if ( LA15_0 == 33 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 1) ) {
                    alt15=2;
                }
                else if ( LA15_0 == 34 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 2) ) {
                    alt15=3;
                }
                else if ( LA15_0 == 35 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 3) ) {
                    alt15=4;
                }
                else if ( LA15_0 == 36 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 4) ) {
                    alt15=5;
                }
                else if ( LA15_0 == 37 && getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 5) ) {
                    alt15=6;
                }


                switch (alt15) {
            	case 1 :
            	    // InternalOseeDsl.g:839:4: ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:839:4: ({...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) ) )
            	    // InternalOseeDsl.g:840:5: {...}? => ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 0) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 0)");
            	    }
            	    // InternalOseeDsl.g:840:112: ( ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) ) )
            	    // InternalOseeDsl.g:841:6: ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 0);
            	    	 				
            	    // InternalOseeDsl.g:844:6: ({...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) ) )
            	    // InternalOseeDsl.g:844:7: {...}? => (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:844:16: (otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) ) )
            	    // InternalOseeDsl.g:844:18: otherlv_16= 'taggerId' ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) )
            	    {
            	    otherlv_16=(Token)match(input,31,FOLLOW_26); 

            	        	newLeafNode(otherlv_16, grammarAccess.getXAttributeTypeAccess().getTaggerIdKeyword_13_0_0());
            	        
            	    // InternalOseeDsl.g:848:1: ( ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) ) )
            	    // InternalOseeDsl.g:849:1: ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) )
            	    {
            	    // InternalOseeDsl.g:849:1: ( (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME ) )
            	    // InternalOseeDsl.g:850:1: (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME )
            	    {
            	    // InternalOseeDsl.g:850:1: (lv_taggerId_17_1= 'DefaultAttributeTaggerProvider' | lv_taggerId_17_2= ruleQUALIFIED_NAME )
            	    int alt14=2;
            	    int LA14_0 = input.LA(1);

            	    if ( (LA14_0==32) ) {
            	        alt14=1;
            	    }
            	    else if ( (LA14_0==RULE_ID) ) {
            	        alt14=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 14, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt14) {
            	        case 1 :
            	            // InternalOseeDsl.g:851:3: lv_taggerId_17_1= 'DefaultAttributeTaggerProvider'
            	            {
            	            lv_taggerId_17_1=(Token)match(input,32,FOLLOW_25); 

            	                    newLeafNode(lv_taggerId_17_1, grammarAccess.getXAttributeTypeAccess().getTaggerIdDefaultAttributeTaggerProviderKeyword_13_0_1_0_0());
            	                

            	            	        if (current==null) {
            	            	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	            	        }
            	                   		setWithLastConsumed(current, "taggerId", lv_taggerId_17_1, null);
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // InternalOseeDsl.g:863:8: lv_taggerId_17_2= ruleQUALIFIED_NAME
            	            {
            	             
            	            	        newCompositeNode(grammarAccess.getXAttributeTypeAccess().getTaggerIdQUALIFIED_NAMEParserRuleCall_13_0_1_0_1()); 
            	            	    
            	            pushFollow(FOLLOW_25);
            	            lv_taggerId_17_2=ruleQUALIFIED_NAME();

            	            state._fsp--;


            	            	        if (current==null) {
            	            	            current = createModelElementForParent(grammarAccess.getXAttributeTypeRule());
            	            	        }
            	                   		set(
            	                   			current, 
            	                   			"taggerId",
            	                    		lv_taggerId_17_2, 
            	                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.QUALIFIED_NAME");
            	            	        afterParserOrEnumRuleCall();
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // InternalOseeDsl.g:888:4: ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:888:4: ({...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) ) )
            	    // InternalOseeDsl.g:889:5: {...}? => ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 1) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 1)");
            	    }
            	    // InternalOseeDsl.g:889:112: ( ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) ) )
            	    // InternalOseeDsl.g:890:6: ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 1);
            	    	 				
            	    // InternalOseeDsl.g:893:6: ({...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) ) )
            	    // InternalOseeDsl.g:893:7: {...}? => (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:893:16: (otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) ) )
            	    // InternalOseeDsl.g:893:18: otherlv_18= 'enumType' ( (otherlv_19= RULE_STRING ) )
            	    {
            	    otherlv_18=(Token)match(input,33,FOLLOW_6); 

            	        	newLeafNode(otherlv_18, grammarAccess.getXAttributeTypeAccess().getEnumTypeKeyword_13_1_0());
            	        
            	    // InternalOseeDsl.g:897:1: ( (otherlv_19= RULE_STRING ) )
            	    // InternalOseeDsl.g:898:1: (otherlv_19= RULE_STRING )
            	    {
            	    // InternalOseeDsl.g:898:1: (otherlv_19= RULE_STRING )
            	    // InternalOseeDsl.g:899:3: otherlv_19= RULE_STRING
            	    {

            	    			if (current==null) {
            	    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	    	        }
            	            
            	    otherlv_19=(Token)match(input,RULE_STRING,FOLLOW_25); 

            	    		newLeafNode(otherlv_19, grammarAccess.getXAttributeTypeAccess().getEnumTypeXOseeEnumTypeCrossReference_13_1_1_0()); 
            	    	

            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;
            	case 3 :
            	    // InternalOseeDsl.g:917:4: ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:917:4: ({...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) ) )
            	    // InternalOseeDsl.g:918:5: {...}? => ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 2) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 2)");
            	    }
            	    // InternalOseeDsl.g:918:112: ( ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) ) )
            	    // InternalOseeDsl.g:919:6: ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 2);
            	    	 				
            	    // InternalOseeDsl.g:922:6: ({...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) ) )
            	    // InternalOseeDsl.g:922:7: {...}? => (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:922:16: (otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) ) )
            	    // InternalOseeDsl.g:922:18: otherlv_20= 'description' ( (lv_description_21_0= RULE_STRING ) )
            	    {
            	    otherlv_20=(Token)match(input,34,FOLLOW_6); 

            	        	newLeafNode(otherlv_20, grammarAccess.getXAttributeTypeAccess().getDescriptionKeyword_13_2_0());
            	        
            	    // InternalOseeDsl.g:926:1: ( (lv_description_21_0= RULE_STRING ) )
            	    // InternalOseeDsl.g:927:1: (lv_description_21_0= RULE_STRING )
            	    {
            	    // InternalOseeDsl.g:927:1: (lv_description_21_0= RULE_STRING )
            	    // InternalOseeDsl.g:928:3: lv_description_21_0= RULE_STRING
            	    {
            	    lv_description_21_0=(Token)match(input,RULE_STRING,FOLLOW_25); 

            	    			newLeafNode(lv_description_21_0, grammarAccess.getXAttributeTypeAccess().getDescriptionSTRINGTerminalRuleCall_13_2_1_0()); 
            	    		

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	    	        }
            	           		setWithLastConsumed(
            	           			current, 
            	           			"description",
            	            		lv_description_21_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    	    

            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;
            	case 4 :
            	    // InternalOseeDsl.g:951:4: ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:951:4: ({...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) ) )
            	    // InternalOseeDsl.g:952:5: {...}? => ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 3) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 3)");
            	    }
            	    // InternalOseeDsl.g:952:112: ( ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) ) )
            	    // InternalOseeDsl.g:953:6: ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 3);
            	    	 				
            	    // InternalOseeDsl.g:956:6: ({...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) ) )
            	    // InternalOseeDsl.g:956:7: {...}? => (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:956:16: (otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) ) )
            	    // InternalOseeDsl.g:956:18: otherlv_22= 'defaultValue' ( (lv_defaultValue_23_0= RULE_STRING ) )
            	    {
            	    otherlv_22=(Token)match(input,35,FOLLOW_6); 

            	        	newLeafNode(otherlv_22, grammarAccess.getXAttributeTypeAccess().getDefaultValueKeyword_13_3_0());
            	        
            	    // InternalOseeDsl.g:960:1: ( (lv_defaultValue_23_0= RULE_STRING ) )
            	    // InternalOseeDsl.g:961:1: (lv_defaultValue_23_0= RULE_STRING )
            	    {
            	    // InternalOseeDsl.g:961:1: (lv_defaultValue_23_0= RULE_STRING )
            	    // InternalOseeDsl.g:962:3: lv_defaultValue_23_0= RULE_STRING
            	    {
            	    lv_defaultValue_23_0=(Token)match(input,RULE_STRING,FOLLOW_25); 

            	    			newLeafNode(lv_defaultValue_23_0, grammarAccess.getXAttributeTypeAccess().getDefaultValueSTRINGTerminalRuleCall_13_3_1_0()); 
            	    		

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	    	        }
            	           		setWithLastConsumed(
            	           			current, 
            	           			"defaultValue",
            	            		lv_defaultValue_23_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    	    

            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;
            	case 5 :
            	    // InternalOseeDsl.g:985:4: ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:985:4: ({...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) ) )
            	    // InternalOseeDsl.g:986:5: {...}? => ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 4) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 4)");
            	    }
            	    // InternalOseeDsl.g:986:112: ( ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) ) )
            	    // InternalOseeDsl.g:987:6: ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 4);
            	    	 				
            	    // InternalOseeDsl.g:990:6: ({...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) ) )
            	    // InternalOseeDsl.g:990:7: {...}? => (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:990:16: (otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) ) )
            	    // InternalOseeDsl.g:990:18: otherlv_24= 'fileExtension' ( (lv_fileExtension_25_0= RULE_STRING ) )
            	    {
            	    otherlv_24=(Token)match(input,36,FOLLOW_6); 

            	        	newLeafNode(otherlv_24, grammarAccess.getXAttributeTypeAccess().getFileExtensionKeyword_13_4_0());
            	        
            	    // InternalOseeDsl.g:994:1: ( (lv_fileExtension_25_0= RULE_STRING ) )
            	    // InternalOseeDsl.g:995:1: (lv_fileExtension_25_0= RULE_STRING )
            	    {
            	    // InternalOseeDsl.g:995:1: (lv_fileExtension_25_0= RULE_STRING )
            	    // InternalOseeDsl.g:996:3: lv_fileExtension_25_0= RULE_STRING
            	    {
            	    lv_fileExtension_25_0=(Token)match(input,RULE_STRING,FOLLOW_25); 

            	    			newLeafNode(lv_fileExtension_25_0, grammarAccess.getXAttributeTypeAccess().getFileExtensionSTRINGTerminalRuleCall_13_4_1_0()); 
            	    		

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	    	        }
            	           		setWithLastConsumed(
            	           			current, 
            	           			"fileExtension",
            	            		lv_fileExtension_25_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    	    

            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;
            	case 6 :
            	    // InternalOseeDsl.g:1019:4: ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) )
            	    {
            	    // InternalOseeDsl.g:1019:4: ({...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) ) )
            	    // InternalOseeDsl.g:1020:5: {...}? => ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) )
            	    {
            	    if ( ! getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 5) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "getUnorderedGroupHelper().canSelect(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 5)");
            	    }
            	    // InternalOseeDsl.g:1020:112: ( ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) ) )
            	    // InternalOseeDsl.g:1021:6: ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) )
            	    {
            	     
            	    	 				  getUnorderedGroupHelper().select(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13(), 5);
            	    	 				
            	    // InternalOseeDsl.g:1024:6: ({...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) ) )
            	    // InternalOseeDsl.g:1024:7: {...}? => (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) )
            	    {
            	    if ( !((true)) ) {
            	        throw new FailedPredicateException(input, "ruleXAttributeType", "true");
            	    }
            	    // InternalOseeDsl.g:1024:16: (otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) ) )
            	    // InternalOseeDsl.g:1024:18: otherlv_26= 'mediaType' ( (lv_mediaType_27_0= RULE_STRING ) )
            	    {
            	    otherlv_26=(Token)match(input,37,FOLLOW_6); 

            	        	newLeafNode(otherlv_26, grammarAccess.getXAttributeTypeAccess().getMediaTypeKeyword_13_5_0());
            	        
            	    // InternalOseeDsl.g:1028:1: ( (lv_mediaType_27_0= RULE_STRING ) )
            	    // InternalOseeDsl.g:1029:1: (lv_mediaType_27_0= RULE_STRING )
            	    {
            	    // InternalOseeDsl.g:1029:1: (lv_mediaType_27_0= RULE_STRING )
            	    // InternalOseeDsl.g:1030:3: lv_mediaType_27_0= RULE_STRING
            	    {
            	    lv_mediaType_27_0=(Token)match(input,RULE_STRING,FOLLOW_25); 

            	    			newLeafNode(lv_mediaType_27_0, grammarAccess.getXAttributeTypeAccess().getMediaTypeSTRINGTerminalRuleCall_13_5_1_0()); 
            	    		

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getXAttributeTypeRule());
            	    	        }
            	           		setWithLastConsumed(
            	           			current, 
            	           			"mediaType",
            	            		lv_mediaType_27_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    	    

            	    }


            	    }


            	    }


            	    }

            	     
            	    	 				  getUnorderedGroupHelper().returnFromSelection(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	    	 				

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }


            }

             
            	  getUnorderedGroupHelper().leave(grammarAccess.getXAttributeTypeAccess().getUnorderedGroup_13());
            	

            }

            otherlv_28=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_28, grammarAccess.getXAttributeTypeAccess().getRightCurlyBracketKeyword_14());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXAttributeType"


    // $ANTLR start "entryRuleAttributeBaseType"
    // InternalOseeDsl.g:1072:1: entryRuleAttributeBaseType returns [String current=null] : iv_ruleAttributeBaseType= ruleAttributeBaseType EOF ;
    public final String entryRuleAttributeBaseType() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleAttributeBaseType = null;


        try {
            // InternalOseeDsl.g:1073:2: (iv_ruleAttributeBaseType= ruleAttributeBaseType EOF )
            // InternalOseeDsl.g:1074:2: iv_ruleAttributeBaseType= ruleAttributeBaseType EOF
            {
             newCompositeNode(grammarAccess.getAttributeBaseTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAttributeBaseType=ruleAttributeBaseType();

            state._fsp--;

             current =iv_ruleAttributeBaseType.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeBaseType"


    // $ANTLR start "ruleAttributeBaseType"
    // InternalOseeDsl.g:1081:1: ruleAttributeBaseType returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'BooleanAttribute' | kw= 'CompressedContentAttribute' | kw= 'DateAttribute' | kw= 'EnumeratedAttribute' | kw= 'FloatingPointAttribute' | kw= 'IntegerAttribute' | kw= 'LongAttribute' | kw= 'JavaObjectAttribute' | kw= 'StringAttribute' | kw= 'ArtifactReferenceAttribute' | kw= 'BranchReferenceAttribute' | kw= 'WordAttribute' | kw= 'OutlineNumberAttribute' | this_QUALIFIED_NAME_13= ruleQUALIFIED_NAME ) ;
    public final AntlrDatatypeRuleToken ruleAttributeBaseType() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_QUALIFIED_NAME_13 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1084:28: ( (kw= 'BooleanAttribute' | kw= 'CompressedContentAttribute' | kw= 'DateAttribute' | kw= 'EnumeratedAttribute' | kw= 'FloatingPointAttribute' | kw= 'IntegerAttribute' | kw= 'LongAttribute' | kw= 'JavaObjectAttribute' | kw= 'StringAttribute' | kw= 'ArtifactReferenceAttribute' | kw= 'BranchReferenceAttribute' | kw= 'WordAttribute' | kw= 'OutlineNumberAttribute' | this_QUALIFIED_NAME_13= ruleQUALIFIED_NAME ) )
            // InternalOseeDsl.g:1085:1: (kw= 'BooleanAttribute' | kw= 'CompressedContentAttribute' | kw= 'DateAttribute' | kw= 'EnumeratedAttribute' | kw= 'FloatingPointAttribute' | kw= 'IntegerAttribute' | kw= 'LongAttribute' | kw= 'JavaObjectAttribute' | kw= 'StringAttribute' | kw= 'ArtifactReferenceAttribute' | kw= 'BranchReferenceAttribute' | kw= 'WordAttribute' | kw= 'OutlineNumberAttribute' | this_QUALIFIED_NAME_13= ruleQUALIFIED_NAME )
            {
            // InternalOseeDsl.g:1085:1: (kw= 'BooleanAttribute' | kw= 'CompressedContentAttribute' | kw= 'DateAttribute' | kw= 'EnumeratedAttribute' | kw= 'FloatingPointAttribute' | kw= 'IntegerAttribute' | kw= 'LongAttribute' | kw= 'JavaObjectAttribute' | kw= 'StringAttribute' | kw= 'ArtifactReferenceAttribute' | kw= 'BranchReferenceAttribute' | kw= 'WordAttribute' | kw= 'OutlineNumberAttribute' | this_QUALIFIED_NAME_13= ruleQUALIFIED_NAME )
            int alt16=14;
            switch ( input.LA(1) ) {
            case 38:
                {
                alt16=1;
                }
                break;
            case 39:
                {
                alt16=2;
                }
                break;
            case 40:
                {
                alt16=3;
                }
                break;
            case 41:
                {
                alt16=4;
                }
                break;
            case 42:
                {
                alt16=5;
                }
                break;
            case 43:
                {
                alt16=6;
                }
                break;
            case 44:
                {
                alt16=7;
                }
                break;
            case 45:
                {
                alt16=8;
                }
                break;
            case 46:
                {
                alt16=9;
                }
                break;
            case 47:
                {
                alt16=10;
                }
                break;
            case 48:
                {
                alt16=11;
                }
                break;
            case 49:
                {
                alt16=12;
                }
                break;
            case 50:
                {
                alt16=13;
                }
                break;
            case RULE_ID:
                {
                alt16=14;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // InternalOseeDsl.g:1086:2: kw= 'BooleanAttribute'
                    {
                    kw=(Token)match(input,38,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getBooleanAttributeKeyword_0()); 
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:1093:2: kw= 'CompressedContentAttribute'
                    {
                    kw=(Token)match(input,39,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getCompressedContentAttributeKeyword_1()); 
                        

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:1100:2: kw= 'DateAttribute'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getDateAttributeKeyword_2()); 
                        

                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:1107:2: kw= 'EnumeratedAttribute'
                    {
                    kw=(Token)match(input,41,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getEnumeratedAttributeKeyword_3()); 
                        

                    }
                    break;
                case 5 :
                    // InternalOseeDsl.g:1114:2: kw= 'FloatingPointAttribute'
                    {
                    kw=(Token)match(input,42,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getFloatingPointAttributeKeyword_4()); 
                        

                    }
                    break;
                case 6 :
                    // InternalOseeDsl.g:1121:2: kw= 'IntegerAttribute'
                    {
                    kw=(Token)match(input,43,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getIntegerAttributeKeyword_5()); 
                        

                    }
                    break;
                case 7 :
                    // InternalOseeDsl.g:1128:2: kw= 'LongAttribute'
                    {
                    kw=(Token)match(input,44,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getLongAttributeKeyword_6()); 
                        

                    }
                    break;
                case 8 :
                    // InternalOseeDsl.g:1135:2: kw= 'JavaObjectAttribute'
                    {
                    kw=(Token)match(input,45,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getJavaObjectAttributeKeyword_7()); 
                        

                    }
                    break;
                case 9 :
                    // InternalOseeDsl.g:1142:2: kw= 'StringAttribute'
                    {
                    kw=(Token)match(input,46,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getStringAttributeKeyword_8()); 
                        

                    }
                    break;
                case 10 :
                    // InternalOseeDsl.g:1149:2: kw= 'ArtifactReferenceAttribute'
                    {
                    kw=(Token)match(input,47,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getArtifactReferenceAttributeKeyword_9()); 
                        

                    }
                    break;
                case 11 :
                    // InternalOseeDsl.g:1156:2: kw= 'BranchReferenceAttribute'
                    {
                    kw=(Token)match(input,48,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getBranchReferenceAttributeKeyword_10()); 
                        

                    }
                    break;
                case 12 :
                    // InternalOseeDsl.g:1163:2: kw= 'WordAttribute'
                    {
                    kw=(Token)match(input,49,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getWordAttributeKeyword_11()); 
                        

                    }
                    break;
                case 13 :
                    // InternalOseeDsl.g:1170:2: kw= 'OutlineNumberAttribute'
                    {
                    kw=(Token)match(input,50,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getAttributeBaseTypeAccess().getOutlineNumberAttributeKeyword_12()); 
                        

                    }
                    break;
                case 14 :
                    // InternalOseeDsl.g:1177:5: this_QUALIFIED_NAME_13= ruleQUALIFIED_NAME
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeBaseTypeAccess().getQUALIFIED_NAMEParserRuleCall_13()); 
                        
                    pushFollow(FOLLOW_2);
                    this_QUALIFIED_NAME_13=ruleQUALIFIED_NAME();

                    state._fsp--;


                    		current.merge(this_QUALIFIED_NAME_13);
                        
                     
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeBaseType"


    // $ANTLR start "entryRuleXOseeEnumType"
    // InternalOseeDsl.g:1195:1: entryRuleXOseeEnumType returns [EObject current=null] : iv_ruleXOseeEnumType= ruleXOseeEnumType EOF ;
    public final EObject entryRuleXOseeEnumType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXOseeEnumType = null;


        try {
            // InternalOseeDsl.g:1196:2: (iv_ruleXOseeEnumType= ruleXOseeEnumType EOF )
            // InternalOseeDsl.g:1197:2: iv_ruleXOseeEnumType= ruleXOseeEnumType EOF
            {
             newCompositeNode(grammarAccess.getXOseeEnumTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXOseeEnumType=ruleXOseeEnumType();

            state._fsp--;

             current =iv_ruleXOseeEnumType; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXOseeEnumType"


    // $ANTLR start "ruleXOseeEnumType"
    // InternalOseeDsl.g:1204:1: ruleXOseeEnumType returns [EObject current=null] : (otherlv_0= 'oseeEnumType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )* otherlv_6= '}' ) ;
    public final EObject ruleXOseeEnumType() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token lv_id_4_0=null;
        Token otherlv_6=null;
        EObject lv_enumEntries_5_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1207:28: ( (otherlv_0= 'oseeEnumType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )* otherlv_6= '}' ) )
            // InternalOseeDsl.g:1208:1: (otherlv_0= 'oseeEnumType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )* otherlv_6= '}' )
            {
            // InternalOseeDsl.g:1208:1: (otherlv_0= 'oseeEnumType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )* otherlv_6= '}' )
            // InternalOseeDsl.g:1208:3: otherlv_0= 'oseeEnumType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )* otherlv_6= '}'
            {
            otherlv_0=(Token)match(input,51,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXOseeEnumTypeAccess().getOseeEnumTypeKeyword_0());
                
            // InternalOseeDsl.g:1212:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:1213:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:1213:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:1214:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_19); 

            			newLeafNode(lv_name_1_0, grammarAccess.getXOseeEnumTypeAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXOseeEnumTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_12); 

                	newLeafNode(otherlv_2, grammarAccess.getXOseeEnumTypeAccess().getLeftCurlyBracketKeyword_2());
                
            otherlv_3=(Token)match(input,19,FOLLOW_13); 

                	newLeafNode(otherlv_3, grammarAccess.getXOseeEnumTypeAccess().getIdKeyword_3());
                
            // InternalOseeDsl.g:1238:1: ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) )
            // InternalOseeDsl.g:1239:1: (lv_id_4_0= RULE_WHOLE_NUM_STR )
            {
            // InternalOseeDsl.g:1239:1: (lv_id_4_0= RULE_WHOLE_NUM_STR )
            // InternalOseeDsl.g:1240:3: lv_id_4_0= RULE_WHOLE_NUM_STR
            {
            lv_id_4_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_27); 

            			newLeafNode(lv_id_4_0, grammarAccess.getXOseeEnumTypeAccess().getIdWHOLE_NUM_STRTerminalRuleCall_4_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXOseeEnumTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"id",
                    		lv_id_4_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
            	    

            }


            }

            // InternalOseeDsl.g:1256:2: ( (lv_enumEntries_5_0= ruleXOseeEnumEntry ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==52) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // InternalOseeDsl.g:1257:1: (lv_enumEntries_5_0= ruleXOseeEnumEntry )
            	    {
            	    // InternalOseeDsl.g:1257:1: (lv_enumEntries_5_0= ruleXOseeEnumEntry )
            	    // InternalOseeDsl.g:1258:3: lv_enumEntries_5_0= ruleXOseeEnumEntry
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXOseeEnumTypeAccess().getEnumEntriesXOseeEnumEntryParserRuleCall_5_0()); 
            	    	    
            	    pushFollow(FOLLOW_27);
            	    lv_enumEntries_5_0=ruleXOseeEnumEntry();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXOseeEnumTypeRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"enumEntries",
            	            		lv_enumEntries_5_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XOseeEnumEntry");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            otherlv_6=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_6, grammarAccess.getXOseeEnumTypeAccess().getRightCurlyBracketKeyword_6());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXOseeEnumType"


    // $ANTLR start "entryRuleXOseeEnumEntry"
    // InternalOseeDsl.g:1286:1: entryRuleXOseeEnumEntry returns [EObject current=null] : iv_ruleXOseeEnumEntry= ruleXOseeEnumEntry EOF ;
    public final EObject entryRuleXOseeEnumEntry() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXOseeEnumEntry = null;


        try {
            // InternalOseeDsl.g:1287:2: (iv_ruleXOseeEnumEntry= ruleXOseeEnumEntry EOF )
            // InternalOseeDsl.g:1288:2: iv_ruleXOseeEnumEntry= ruleXOseeEnumEntry EOF
            {
             newCompositeNode(grammarAccess.getXOseeEnumEntryRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXOseeEnumEntry=ruleXOseeEnumEntry();

            state._fsp--;

             current =iv_ruleXOseeEnumEntry; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXOseeEnumEntry"


    // $ANTLR start "ruleXOseeEnumEntry"
    // InternalOseeDsl.g:1295:1: ruleXOseeEnumEntry returns [EObject current=null] : (otherlv_0= 'entry' ( (lv_name_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? ) ;
    public final EObject ruleXOseeEnumEntry() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token lv_ordinal_2_0=null;
        Token otherlv_3=null;
        Token lv_entryGuid_4_0=null;
        Token otherlv_5=null;
        Token lv_description_6_0=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1298:28: ( (otherlv_0= 'entry' ( (lv_name_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? ) )
            // InternalOseeDsl.g:1299:1: (otherlv_0= 'entry' ( (lv_name_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? )
            {
            // InternalOseeDsl.g:1299:1: (otherlv_0= 'entry' ( (lv_name_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? )
            // InternalOseeDsl.g:1299:3: otherlv_0= 'entry' ( (lv_name_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )?
            {
            otherlv_0=(Token)match(input,52,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXOseeEnumEntryAccess().getEntryKeyword_0());
                
            // InternalOseeDsl.g:1303:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:1304:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:1304:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:1305:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_28); 

            			newLeafNode(lv_name_1_0, grammarAccess.getXOseeEnumEntryAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXOseeEnumEntryRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:1321:2: ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==RULE_WHOLE_NUM_STR) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // InternalOseeDsl.g:1322:1: (lv_ordinal_2_0= RULE_WHOLE_NUM_STR )
                    {
                    // InternalOseeDsl.g:1322:1: (lv_ordinal_2_0= RULE_WHOLE_NUM_STR )
                    // InternalOseeDsl.g:1323:3: lv_ordinal_2_0= RULE_WHOLE_NUM_STR
                    {
                    lv_ordinal_2_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_29); 

                    			newLeafNode(lv_ordinal_2_0, grammarAccess.getXOseeEnumEntryAccess().getOrdinalWHOLE_NUM_STRTerminalRuleCall_2_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXOseeEnumEntryRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"ordinal",
                            		lv_ordinal_2_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
                    	    

                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1339:3: (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==53) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // InternalOseeDsl.g:1339:5: otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) )
                    {
                    otherlv_3=(Token)match(input,53,FOLLOW_6); 

                        	newLeafNode(otherlv_3, grammarAccess.getXOseeEnumEntryAccess().getEntryGuidKeyword_3_0());
                        
                    // InternalOseeDsl.g:1343:1: ( (lv_entryGuid_4_0= RULE_STRING ) )
                    // InternalOseeDsl.g:1344:1: (lv_entryGuid_4_0= RULE_STRING )
                    {
                    // InternalOseeDsl.g:1344:1: (lv_entryGuid_4_0= RULE_STRING )
                    // InternalOseeDsl.g:1345:3: lv_entryGuid_4_0= RULE_STRING
                    {
                    lv_entryGuid_4_0=(Token)match(input,RULE_STRING,FOLLOW_30); 

                    			newLeafNode(lv_entryGuid_4_0, grammarAccess.getXOseeEnumEntryAccess().getEntryGuidSTRINGTerminalRuleCall_3_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXOseeEnumEntryRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"entryGuid",
                            		lv_entryGuid_4_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
                    	    

                    }


                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1361:4: (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==34) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalOseeDsl.g:1361:6: otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) )
                    {
                    otherlv_5=(Token)match(input,34,FOLLOW_6); 

                        	newLeafNode(otherlv_5, grammarAccess.getXOseeEnumEntryAccess().getDescriptionKeyword_4_0());
                        
                    // InternalOseeDsl.g:1365:1: ( (lv_description_6_0= RULE_STRING ) )
                    // InternalOseeDsl.g:1366:1: (lv_description_6_0= RULE_STRING )
                    {
                    // InternalOseeDsl.g:1366:1: (lv_description_6_0= RULE_STRING )
                    // InternalOseeDsl.g:1367:3: lv_description_6_0= RULE_STRING
                    {
                    lv_description_6_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

                    			newLeafNode(lv_description_6_0, grammarAccess.getXOseeEnumEntryAccess().getDescriptionSTRINGTerminalRuleCall_4_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXOseeEnumEntryRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"description",
                            		lv_description_6_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
                    	    

                    }


                    }


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXOseeEnumEntry"


    // $ANTLR start "entryRuleXOseeEnumOverride"
    // InternalOseeDsl.g:1391:1: entryRuleXOseeEnumOverride returns [EObject current=null] : iv_ruleXOseeEnumOverride= ruleXOseeEnumOverride EOF ;
    public final EObject entryRuleXOseeEnumOverride() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXOseeEnumOverride = null;


        try {
            // InternalOseeDsl.g:1392:2: (iv_ruleXOseeEnumOverride= ruleXOseeEnumOverride EOF )
            // InternalOseeDsl.g:1393:2: iv_ruleXOseeEnumOverride= ruleXOseeEnumOverride EOF
            {
             newCompositeNode(grammarAccess.getXOseeEnumOverrideRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXOseeEnumOverride=ruleXOseeEnumOverride();

            state._fsp--;

             current =iv_ruleXOseeEnumOverride; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXOseeEnumOverride"


    // $ANTLR start "ruleXOseeEnumOverride"
    // InternalOseeDsl.g:1400:1: ruleXOseeEnumOverride returns [EObject current=null] : (otherlv_0= 'overrides enum' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleOverrideOption ) )* otherlv_5= '}' ) ;
    public final EObject ruleXOseeEnumOverride() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;
        Token lv_inheritAll_3_0=null;
        Token otherlv_5=null;
        EObject lv_overrideOptions_4_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1403:28: ( (otherlv_0= 'overrides enum' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleOverrideOption ) )* otherlv_5= '}' ) )
            // InternalOseeDsl.g:1404:1: (otherlv_0= 'overrides enum' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleOverrideOption ) )* otherlv_5= '}' )
            {
            // InternalOseeDsl.g:1404:1: (otherlv_0= 'overrides enum' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleOverrideOption ) )* otherlv_5= '}' )
            // InternalOseeDsl.g:1404:3: otherlv_0= 'overrides enum' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleOverrideOption ) )* otherlv_5= '}'
            {
            otherlv_0=(Token)match(input,54,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXOseeEnumOverrideAccess().getOverridesEnumKeyword_0());
                
            // InternalOseeDsl.g:1408:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:1409:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:1409:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:1410:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getXOseeEnumOverrideRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_19); 

            		newLeafNode(otherlv_1, grammarAccess.getXOseeEnumOverrideAccess().getOverridenEnumTypeXOseeEnumTypeCrossReference_1_0()); 
            	

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_31); 

                	newLeafNode(otherlv_2, grammarAccess.getXOseeEnumOverrideAccess().getLeftCurlyBracketKeyword_2());
                
            // InternalOseeDsl.g:1425:1: ( (lv_inheritAll_3_0= 'inheritAll' ) )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==55) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // InternalOseeDsl.g:1426:1: (lv_inheritAll_3_0= 'inheritAll' )
                    {
                    // InternalOseeDsl.g:1426:1: (lv_inheritAll_3_0= 'inheritAll' )
                    // InternalOseeDsl.g:1427:3: lv_inheritAll_3_0= 'inheritAll'
                    {
                    lv_inheritAll_3_0=(Token)match(input,55,FOLLOW_32); 

                            newLeafNode(lv_inheritAll_3_0, grammarAccess.getXOseeEnumOverrideAccess().getInheritAllInheritAllKeyword_3_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXOseeEnumOverrideRule());
                    	        }
                           		setWithLastConsumed(current, "inheritAll", true, "inheritAll");
                    	    

                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1440:3: ( (lv_overrideOptions_4_0= ruleOverrideOption ) )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>=56 && LA22_0<=57)) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // InternalOseeDsl.g:1441:1: (lv_overrideOptions_4_0= ruleOverrideOption )
            	    {
            	    // InternalOseeDsl.g:1441:1: (lv_overrideOptions_4_0= ruleOverrideOption )
            	    // InternalOseeDsl.g:1442:3: lv_overrideOptions_4_0= ruleOverrideOption
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXOseeEnumOverrideAccess().getOverrideOptionsOverrideOptionParserRuleCall_4_0()); 
            	    	    
            	    pushFollow(FOLLOW_32);
            	    lv_overrideOptions_4_0=ruleOverrideOption();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXOseeEnumOverrideRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"overrideOptions",
            	            		lv_overrideOptions_4_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.OverrideOption");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            otherlv_5=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_5, grammarAccess.getXOseeEnumOverrideAccess().getRightCurlyBracketKeyword_5());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXOseeEnumOverride"


    // $ANTLR start "entryRuleOverrideOption"
    // InternalOseeDsl.g:1470:1: entryRuleOverrideOption returns [EObject current=null] : iv_ruleOverrideOption= ruleOverrideOption EOF ;
    public final EObject entryRuleOverrideOption() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOverrideOption = null;


        try {
            // InternalOseeDsl.g:1471:2: (iv_ruleOverrideOption= ruleOverrideOption EOF )
            // InternalOseeDsl.g:1472:2: iv_ruleOverrideOption= ruleOverrideOption EOF
            {
             newCompositeNode(grammarAccess.getOverrideOptionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOverrideOption=ruleOverrideOption();

            state._fsp--;

             current =iv_ruleOverrideOption; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOverrideOption"


    // $ANTLR start "ruleOverrideOption"
    // InternalOseeDsl.g:1479:1: ruleOverrideOption returns [EObject current=null] : (this_AddEnum_0= ruleAddEnum | this_RemoveEnum_1= ruleRemoveEnum ) ;
    public final EObject ruleOverrideOption() throws RecognitionException {
        EObject current = null;

        EObject this_AddEnum_0 = null;

        EObject this_RemoveEnum_1 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1482:28: ( (this_AddEnum_0= ruleAddEnum | this_RemoveEnum_1= ruleRemoveEnum ) )
            // InternalOseeDsl.g:1483:1: (this_AddEnum_0= ruleAddEnum | this_RemoveEnum_1= ruleRemoveEnum )
            {
            // InternalOseeDsl.g:1483:1: (this_AddEnum_0= ruleAddEnum | this_RemoveEnum_1= ruleRemoveEnum )
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==56) ) {
                alt23=1;
            }
            else if ( (LA23_0==57) ) {
                alt23=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // InternalOseeDsl.g:1484:5: this_AddEnum_0= ruleAddEnum
                    {
                     
                            newCompositeNode(grammarAccess.getOverrideOptionAccess().getAddEnumParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_AddEnum_0=ruleAddEnum();

                    state._fsp--;

                     
                            current = this_AddEnum_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:1494:5: this_RemoveEnum_1= ruleRemoveEnum
                    {
                     
                            newCompositeNode(grammarAccess.getOverrideOptionAccess().getRemoveEnumParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_RemoveEnum_1=ruleRemoveEnum();

                    state._fsp--;

                     
                            current = this_RemoveEnum_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOverrideOption"


    // $ANTLR start "entryRuleAddEnum"
    // InternalOseeDsl.g:1510:1: entryRuleAddEnum returns [EObject current=null] : iv_ruleAddEnum= ruleAddEnum EOF ;
    public final EObject entryRuleAddEnum() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddEnum = null;


        try {
            // InternalOseeDsl.g:1511:2: (iv_ruleAddEnum= ruleAddEnum EOF )
            // InternalOseeDsl.g:1512:2: iv_ruleAddEnum= ruleAddEnum EOF
            {
             newCompositeNode(grammarAccess.getAddEnumRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAddEnum=ruleAddEnum();

            state._fsp--;

             current =iv_ruleAddEnum; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAddEnum"


    // $ANTLR start "ruleAddEnum"
    // InternalOseeDsl.g:1519:1: ruleAddEnum returns [EObject current=null] : (otherlv_0= 'add' ( (lv_enumEntry_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? ) ;
    public final EObject ruleAddEnum() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_enumEntry_1_0=null;
        Token lv_ordinal_2_0=null;
        Token otherlv_3=null;
        Token lv_entryGuid_4_0=null;
        Token otherlv_5=null;
        Token lv_description_6_0=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1522:28: ( (otherlv_0= 'add' ( (lv_enumEntry_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? ) )
            // InternalOseeDsl.g:1523:1: (otherlv_0= 'add' ( (lv_enumEntry_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? )
            {
            // InternalOseeDsl.g:1523:1: (otherlv_0= 'add' ( (lv_enumEntry_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )? )
            // InternalOseeDsl.g:1523:3: otherlv_0= 'add' ( (lv_enumEntry_1_0= RULE_STRING ) ) ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )? (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )? (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )?
            {
            otherlv_0=(Token)match(input,56,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getAddEnumAccess().getAddKeyword_0());
                
            // InternalOseeDsl.g:1527:1: ( (lv_enumEntry_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:1528:1: (lv_enumEntry_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:1528:1: (lv_enumEntry_1_0= RULE_STRING )
            // InternalOseeDsl.g:1529:3: lv_enumEntry_1_0= RULE_STRING
            {
            lv_enumEntry_1_0=(Token)match(input,RULE_STRING,FOLLOW_28); 

            			newLeafNode(lv_enumEntry_1_0, grammarAccess.getAddEnumAccess().getEnumEntrySTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getAddEnumRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"enumEntry",
                    		lv_enumEntry_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:1545:2: ( (lv_ordinal_2_0= RULE_WHOLE_NUM_STR ) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==RULE_WHOLE_NUM_STR) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalOseeDsl.g:1546:1: (lv_ordinal_2_0= RULE_WHOLE_NUM_STR )
                    {
                    // InternalOseeDsl.g:1546:1: (lv_ordinal_2_0= RULE_WHOLE_NUM_STR )
                    // InternalOseeDsl.g:1547:3: lv_ordinal_2_0= RULE_WHOLE_NUM_STR
                    {
                    lv_ordinal_2_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_29); 

                    			newLeafNode(lv_ordinal_2_0, grammarAccess.getAddEnumAccess().getOrdinalWHOLE_NUM_STRTerminalRuleCall_2_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getAddEnumRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"ordinal",
                            		lv_ordinal_2_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
                    	    

                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1563:3: (otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) ) )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==53) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // InternalOseeDsl.g:1563:5: otherlv_3= 'entryGuid' ( (lv_entryGuid_4_0= RULE_STRING ) )
                    {
                    otherlv_3=(Token)match(input,53,FOLLOW_6); 

                        	newLeafNode(otherlv_3, grammarAccess.getAddEnumAccess().getEntryGuidKeyword_3_0());
                        
                    // InternalOseeDsl.g:1567:1: ( (lv_entryGuid_4_0= RULE_STRING ) )
                    // InternalOseeDsl.g:1568:1: (lv_entryGuid_4_0= RULE_STRING )
                    {
                    // InternalOseeDsl.g:1568:1: (lv_entryGuid_4_0= RULE_STRING )
                    // InternalOseeDsl.g:1569:3: lv_entryGuid_4_0= RULE_STRING
                    {
                    lv_entryGuid_4_0=(Token)match(input,RULE_STRING,FOLLOW_30); 

                    			newLeafNode(lv_entryGuid_4_0, grammarAccess.getAddEnumAccess().getEntryGuidSTRINGTerminalRuleCall_3_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getAddEnumRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"entryGuid",
                            		lv_entryGuid_4_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
                    	    

                    }


                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1585:4: (otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) ) )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==34) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // InternalOseeDsl.g:1585:6: otherlv_5= 'description' ( (lv_description_6_0= RULE_STRING ) )
                    {
                    otherlv_5=(Token)match(input,34,FOLLOW_6); 

                        	newLeafNode(otherlv_5, grammarAccess.getAddEnumAccess().getDescriptionKeyword_4_0());
                        
                    // InternalOseeDsl.g:1589:1: ( (lv_description_6_0= RULE_STRING ) )
                    // InternalOseeDsl.g:1590:1: (lv_description_6_0= RULE_STRING )
                    {
                    // InternalOseeDsl.g:1590:1: (lv_description_6_0= RULE_STRING )
                    // InternalOseeDsl.g:1591:3: lv_description_6_0= RULE_STRING
                    {
                    lv_description_6_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

                    			newLeafNode(lv_description_6_0, grammarAccess.getAddEnumAccess().getDescriptionSTRINGTerminalRuleCall_4_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getAddEnumRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"description",
                            		lv_description_6_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
                    	    

                    }


                    }


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAddEnum"


    // $ANTLR start "entryRuleRemoveEnum"
    // InternalOseeDsl.g:1615:1: entryRuleRemoveEnum returns [EObject current=null] : iv_ruleRemoveEnum= ruleRemoveEnum EOF ;
    public final EObject entryRuleRemoveEnum() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRemoveEnum = null;


        try {
            // InternalOseeDsl.g:1616:2: (iv_ruleRemoveEnum= ruleRemoveEnum EOF )
            // InternalOseeDsl.g:1617:2: iv_ruleRemoveEnum= ruleRemoveEnum EOF
            {
             newCompositeNode(grammarAccess.getRemoveEnumRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRemoveEnum=ruleRemoveEnum();

            state._fsp--;

             current =iv_ruleRemoveEnum; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRemoveEnum"


    // $ANTLR start "ruleRemoveEnum"
    // InternalOseeDsl.g:1624:1: ruleRemoveEnum returns [EObject current=null] : (otherlv_0= 'remove' ( (otherlv_1= RULE_STRING ) ) ) ;
    public final EObject ruleRemoveEnum() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1627:28: ( (otherlv_0= 'remove' ( (otherlv_1= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:1628:1: (otherlv_0= 'remove' ( (otherlv_1= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:1628:1: (otherlv_0= 'remove' ( (otherlv_1= RULE_STRING ) ) )
            // InternalOseeDsl.g:1628:3: otherlv_0= 'remove' ( (otherlv_1= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,57,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getRemoveEnumAccess().getRemoveKeyword_0());
                
            // InternalOseeDsl.g:1632:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:1633:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:1633:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:1634:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getRemoveEnumRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		newLeafNode(otherlv_1, grammarAccess.getRemoveEnumAccess().getEnumEntryXOseeEnumEntryCrossReference_1_0()); 
            	

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRemoveEnum"


    // $ANTLR start "entryRuleXOseeArtifactTypeOverride"
    // InternalOseeDsl.g:1653:1: entryRuleXOseeArtifactTypeOverride returns [EObject current=null] : iv_ruleXOseeArtifactTypeOverride= ruleXOseeArtifactTypeOverride EOF ;
    public final EObject entryRuleXOseeArtifactTypeOverride() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXOseeArtifactTypeOverride = null;


        try {
            // InternalOseeDsl.g:1654:2: (iv_ruleXOseeArtifactTypeOverride= ruleXOseeArtifactTypeOverride EOF )
            // InternalOseeDsl.g:1655:2: iv_ruleXOseeArtifactTypeOverride= ruleXOseeArtifactTypeOverride EOF
            {
             newCompositeNode(grammarAccess.getXOseeArtifactTypeOverrideRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXOseeArtifactTypeOverride=ruleXOseeArtifactTypeOverride();

            state._fsp--;

             current =iv_ruleXOseeArtifactTypeOverride; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXOseeArtifactTypeOverride"


    // $ANTLR start "ruleXOseeArtifactTypeOverride"
    // InternalOseeDsl.g:1662:1: ruleXOseeArtifactTypeOverride returns [EObject current=null] : (otherlv_0= 'overrides artifactType' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+ otherlv_5= '}' ) ;
    public final EObject ruleXOseeArtifactTypeOverride() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;
        Token lv_inheritAll_3_0=null;
        Token otherlv_5=null;
        EObject lv_overrideOptions_4_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1665:28: ( (otherlv_0= 'overrides artifactType' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+ otherlv_5= '}' ) )
            // InternalOseeDsl.g:1666:1: (otherlv_0= 'overrides artifactType' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+ otherlv_5= '}' )
            {
            // InternalOseeDsl.g:1666:1: (otherlv_0= 'overrides artifactType' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+ otherlv_5= '}' )
            // InternalOseeDsl.g:1666:3: otherlv_0= 'overrides artifactType' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_inheritAll_3_0= 'inheritAll' ) )? ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+ otherlv_5= '}'
            {
            otherlv_0=(Token)match(input,58,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXOseeArtifactTypeOverrideAccess().getOverridesArtifactTypeKeyword_0());
                
            // InternalOseeDsl.g:1670:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:1671:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:1671:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:1672:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getXOseeArtifactTypeOverrideRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_19); 

            		newLeafNode(otherlv_1, grammarAccess.getXOseeArtifactTypeOverrideAccess().getOverridenArtifactTypeXArtifactTypeCrossReference_1_0()); 
            	

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_33); 

                	newLeafNode(otherlv_2, grammarAccess.getXOseeArtifactTypeOverrideAccess().getLeftCurlyBracketKeyword_2());
                
            // InternalOseeDsl.g:1687:1: ( (lv_inheritAll_3_0= 'inheritAll' ) )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==55) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalOseeDsl.g:1688:1: (lv_inheritAll_3_0= 'inheritAll' )
                    {
                    // InternalOseeDsl.g:1688:1: (lv_inheritAll_3_0= 'inheritAll' )
                    // InternalOseeDsl.g:1689:3: lv_inheritAll_3_0= 'inheritAll'
                    {
                    lv_inheritAll_3_0=(Token)match(input,55,FOLLOW_33); 

                            newLeafNode(lv_inheritAll_3_0, grammarAccess.getXOseeArtifactTypeOverrideAccess().getInheritAllInheritAllKeyword_3_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getXOseeArtifactTypeOverrideRule());
                    	        }
                           		setWithLastConsumed(current, "inheritAll", true, "inheritAll");
                    	    

                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:1702:3: ( (lv_overrideOptions_4_0= ruleAttributeOverrideOption ) )+
            int cnt28=0;
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( ((LA28_0>=56 && LA28_0<=57)||LA28_0==59) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // InternalOseeDsl.g:1703:1: (lv_overrideOptions_4_0= ruleAttributeOverrideOption )
            	    {
            	    // InternalOseeDsl.g:1703:1: (lv_overrideOptions_4_0= ruleAttributeOverrideOption )
            	    // InternalOseeDsl.g:1704:3: lv_overrideOptions_4_0= ruleAttributeOverrideOption
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXOseeArtifactTypeOverrideAccess().getOverrideOptionsAttributeOverrideOptionParserRuleCall_4_0()); 
            	    	    
            	    pushFollow(FOLLOW_34);
            	    lv_overrideOptions_4_0=ruleAttributeOverrideOption();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXOseeArtifactTypeOverrideRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"overrideOptions",
            	            		lv_overrideOptions_4_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.AttributeOverrideOption");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt28 >= 1 ) break loop28;
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
            } while (true);

            otherlv_5=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_5, grammarAccess.getXOseeArtifactTypeOverrideAccess().getRightCurlyBracketKeyword_5());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXOseeArtifactTypeOverride"


    // $ANTLR start "entryRuleAttributeOverrideOption"
    // InternalOseeDsl.g:1732:1: entryRuleAttributeOverrideOption returns [EObject current=null] : iv_ruleAttributeOverrideOption= ruleAttributeOverrideOption EOF ;
    public final EObject entryRuleAttributeOverrideOption() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeOverrideOption = null;


        try {
            // InternalOseeDsl.g:1733:2: (iv_ruleAttributeOverrideOption= ruleAttributeOverrideOption EOF )
            // InternalOseeDsl.g:1734:2: iv_ruleAttributeOverrideOption= ruleAttributeOverrideOption EOF
            {
             newCompositeNode(grammarAccess.getAttributeOverrideOptionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAttributeOverrideOption=ruleAttributeOverrideOption();

            state._fsp--;

             current =iv_ruleAttributeOverrideOption; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeOverrideOption"


    // $ANTLR start "ruleAttributeOverrideOption"
    // InternalOseeDsl.g:1741:1: ruleAttributeOverrideOption returns [EObject current=null] : (this_AddAttribute_0= ruleAddAttribute | this_RemoveAttribute_1= ruleRemoveAttribute | this_UpdateAttribute_2= ruleUpdateAttribute ) ;
    public final EObject ruleAttributeOverrideOption() throws RecognitionException {
        EObject current = null;

        EObject this_AddAttribute_0 = null;

        EObject this_RemoveAttribute_1 = null;

        EObject this_UpdateAttribute_2 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1744:28: ( (this_AddAttribute_0= ruleAddAttribute | this_RemoveAttribute_1= ruleRemoveAttribute | this_UpdateAttribute_2= ruleUpdateAttribute ) )
            // InternalOseeDsl.g:1745:1: (this_AddAttribute_0= ruleAddAttribute | this_RemoveAttribute_1= ruleRemoveAttribute | this_UpdateAttribute_2= ruleUpdateAttribute )
            {
            // InternalOseeDsl.g:1745:1: (this_AddAttribute_0= ruleAddAttribute | this_RemoveAttribute_1= ruleRemoveAttribute | this_UpdateAttribute_2= ruleUpdateAttribute )
            int alt29=3;
            switch ( input.LA(1) ) {
            case 56:
                {
                alt29=1;
                }
                break;
            case 57:
                {
                alt29=2;
                }
                break;
            case 59:
                {
                alt29=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }

            switch (alt29) {
                case 1 :
                    // InternalOseeDsl.g:1746:5: this_AddAttribute_0= ruleAddAttribute
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeOverrideOptionAccess().getAddAttributeParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_AddAttribute_0=ruleAddAttribute();

                    state._fsp--;

                     
                            current = this_AddAttribute_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:1756:5: this_RemoveAttribute_1= ruleRemoveAttribute
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeOverrideOptionAccess().getRemoveAttributeParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_RemoveAttribute_1=ruleRemoveAttribute();

                    state._fsp--;

                     
                            current = this_RemoveAttribute_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:1766:5: this_UpdateAttribute_2= ruleUpdateAttribute
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeOverrideOptionAccess().getUpdateAttributeParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_2);
                    this_UpdateAttribute_2=ruleUpdateAttribute();

                    state._fsp--;

                     
                            current = this_UpdateAttribute_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeOverrideOption"


    // $ANTLR start "entryRuleAddAttribute"
    // InternalOseeDsl.g:1782:1: entryRuleAddAttribute returns [EObject current=null] : iv_ruleAddAttribute= ruleAddAttribute EOF ;
    public final EObject entryRuleAddAttribute() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddAttribute = null;


        try {
            // InternalOseeDsl.g:1783:2: (iv_ruleAddAttribute= ruleAddAttribute EOF )
            // InternalOseeDsl.g:1784:2: iv_ruleAddAttribute= ruleAddAttribute EOF
            {
             newCompositeNode(grammarAccess.getAddAttributeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAddAttribute=ruleAddAttribute();

            state._fsp--;

             current =iv_ruleAddAttribute; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAddAttribute"


    // $ANTLR start "ruleAddAttribute"
    // InternalOseeDsl.g:1791:1: ruleAddAttribute returns [EObject current=null] : (otherlv_0= 'add' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) ) ;
    public final EObject ruleAddAttribute() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_attribute_1_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1794:28: ( (otherlv_0= 'add' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) ) )
            // InternalOseeDsl.g:1795:1: (otherlv_0= 'add' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) )
            {
            // InternalOseeDsl.g:1795:1: (otherlv_0= 'add' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) )
            // InternalOseeDsl.g:1795:3: otherlv_0= 'add' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) )
            {
            otherlv_0=(Token)match(input,56,FOLLOW_35); 

                	newLeafNode(otherlv_0, grammarAccess.getAddAttributeAccess().getAddKeyword_0());
                
            // InternalOseeDsl.g:1799:1: ( (lv_attribute_1_0= ruleXAttributeTypeRef ) )
            // InternalOseeDsl.g:1800:1: (lv_attribute_1_0= ruleXAttributeTypeRef )
            {
            // InternalOseeDsl.g:1800:1: (lv_attribute_1_0= ruleXAttributeTypeRef )
            // InternalOseeDsl.g:1801:3: lv_attribute_1_0= ruleXAttributeTypeRef
            {
             
            	        newCompositeNode(grammarAccess.getAddAttributeAccess().getAttributeXAttributeTypeRefParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_2);
            lv_attribute_1_0=ruleXAttributeTypeRef();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAddAttributeRule());
            	        }
                   		set(
                   			current, 
                   			"attribute",
                    		lv_attribute_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.XAttributeTypeRef");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAddAttribute"


    // $ANTLR start "entryRuleRemoveAttribute"
    // InternalOseeDsl.g:1825:1: entryRuleRemoveAttribute returns [EObject current=null] : iv_ruleRemoveAttribute= ruleRemoveAttribute EOF ;
    public final EObject entryRuleRemoveAttribute() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRemoveAttribute = null;


        try {
            // InternalOseeDsl.g:1826:2: (iv_ruleRemoveAttribute= ruleRemoveAttribute EOF )
            // InternalOseeDsl.g:1827:2: iv_ruleRemoveAttribute= ruleRemoveAttribute EOF
            {
             newCompositeNode(grammarAccess.getRemoveAttributeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRemoveAttribute=ruleRemoveAttribute();

            state._fsp--;

             current =iv_ruleRemoveAttribute; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRemoveAttribute"


    // $ANTLR start "ruleRemoveAttribute"
    // InternalOseeDsl.g:1834:1: ruleRemoveAttribute returns [EObject current=null] : (otherlv_0= 'remove' otherlv_1= 'attribute' ( (otherlv_2= RULE_STRING ) ) ) ;
    public final EObject ruleRemoveAttribute() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1837:28: ( (otherlv_0= 'remove' otherlv_1= 'attribute' ( (otherlv_2= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:1838:1: (otherlv_0= 'remove' otherlv_1= 'attribute' ( (otherlv_2= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:1838:1: (otherlv_0= 'remove' otherlv_1= 'attribute' ( (otherlv_2= RULE_STRING ) ) )
            // InternalOseeDsl.g:1838:3: otherlv_0= 'remove' otherlv_1= 'attribute' ( (otherlv_2= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,57,FOLLOW_35); 

                	newLeafNode(otherlv_0, grammarAccess.getRemoveAttributeAccess().getRemoveKeyword_0());
                
            otherlv_1=(Token)match(input,21,FOLLOW_6); 

                	newLeafNode(otherlv_1, grammarAccess.getRemoveAttributeAccess().getAttributeKeyword_1());
                
            // InternalOseeDsl.g:1846:1: ( (otherlv_2= RULE_STRING ) )
            // InternalOseeDsl.g:1847:1: (otherlv_2= RULE_STRING )
            {
            // InternalOseeDsl.g:1847:1: (otherlv_2= RULE_STRING )
            // InternalOseeDsl.g:1848:3: otherlv_2= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getRemoveAttributeRule());
            	        }
                    
            otherlv_2=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		newLeafNode(otherlv_2, grammarAccess.getRemoveAttributeAccess().getAttributeXAttributeTypeCrossReference_2_0()); 
            	

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRemoveAttribute"


    // $ANTLR start "entryRuleUpdateAttribute"
    // InternalOseeDsl.g:1867:1: entryRuleUpdateAttribute returns [EObject current=null] : iv_ruleUpdateAttribute= ruleUpdateAttribute EOF ;
    public final EObject entryRuleUpdateAttribute() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUpdateAttribute = null;


        try {
            // InternalOseeDsl.g:1868:2: (iv_ruleUpdateAttribute= ruleUpdateAttribute EOF )
            // InternalOseeDsl.g:1869:2: iv_ruleUpdateAttribute= ruleUpdateAttribute EOF
            {
             newCompositeNode(grammarAccess.getUpdateAttributeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleUpdateAttribute=ruleUpdateAttribute();

            state._fsp--;

             current =iv_ruleUpdateAttribute; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUpdateAttribute"


    // $ANTLR start "ruleUpdateAttribute"
    // InternalOseeDsl.g:1876:1: ruleUpdateAttribute returns [EObject current=null] : (otherlv_0= 'update' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) ) ;
    public final EObject ruleUpdateAttribute() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_attribute_1_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1879:28: ( (otherlv_0= 'update' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) ) )
            // InternalOseeDsl.g:1880:1: (otherlv_0= 'update' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) )
            {
            // InternalOseeDsl.g:1880:1: (otherlv_0= 'update' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) ) )
            // InternalOseeDsl.g:1880:3: otherlv_0= 'update' ( (lv_attribute_1_0= ruleXAttributeTypeRef ) )
            {
            otherlv_0=(Token)match(input,59,FOLLOW_35); 

                	newLeafNode(otherlv_0, grammarAccess.getUpdateAttributeAccess().getUpdateKeyword_0());
                
            // InternalOseeDsl.g:1884:1: ( (lv_attribute_1_0= ruleXAttributeTypeRef ) )
            // InternalOseeDsl.g:1885:1: (lv_attribute_1_0= ruleXAttributeTypeRef )
            {
            // InternalOseeDsl.g:1885:1: (lv_attribute_1_0= ruleXAttributeTypeRef )
            // InternalOseeDsl.g:1886:3: lv_attribute_1_0= ruleXAttributeTypeRef
            {
             
            	        newCompositeNode(grammarAccess.getUpdateAttributeAccess().getAttributeXAttributeTypeRefParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_2);
            lv_attribute_1_0=ruleXAttributeTypeRef();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getUpdateAttributeRule());
            	        }
                   		set(
                   			current, 
                   			"attribute",
                    		lv_attribute_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.XAttributeTypeRef");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUpdateAttribute"


    // $ANTLR start "entryRuleXRelationType"
    // InternalOseeDsl.g:1910:1: entryRuleXRelationType returns [EObject current=null] : iv_ruleXRelationType= ruleXRelationType EOF ;
    public final EObject entryRuleXRelationType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXRelationType = null;


        try {
            // InternalOseeDsl.g:1911:2: (iv_ruleXRelationType= ruleXRelationType EOF )
            // InternalOseeDsl.g:1912:2: iv_ruleXRelationType= ruleXRelationType EOF
            {
             newCompositeNode(grammarAccess.getXRelationTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXRelationType=ruleXRelationType();

            state._fsp--;

             current =iv_ruleXRelationType; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXRelationType"


    // $ANTLR start "ruleXRelationType"
    // InternalOseeDsl.g:1919:1: ruleXRelationType returns [EObject current=null] : (otherlv_0= 'relationType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) otherlv_5= 'sideAName' ( (lv_sideAName_6_0= RULE_STRING ) ) otherlv_7= 'sideAArtifactType' ( (otherlv_8= RULE_STRING ) ) otherlv_9= 'sideBName' ( (lv_sideBName_10_0= RULE_STRING ) ) otherlv_11= 'sideBArtifactType' ( (otherlv_12= RULE_STRING ) ) otherlv_13= 'defaultOrderType' ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) ) otherlv_15= 'multiplicity' ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) ) otherlv_17= '}' ) ;
    public final EObject ruleXRelationType() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token lv_id_4_0=null;
        Token otherlv_5=null;
        Token lv_sideAName_6_0=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token lv_sideBName_10_0=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        AntlrDatatypeRuleToken lv_defaultOrderType_14_0 = null;

        Enumerator lv_multiplicity_16_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:1922:28: ( (otherlv_0= 'relationType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) otherlv_5= 'sideAName' ( (lv_sideAName_6_0= RULE_STRING ) ) otherlv_7= 'sideAArtifactType' ( (otherlv_8= RULE_STRING ) ) otherlv_9= 'sideBName' ( (lv_sideBName_10_0= RULE_STRING ) ) otherlv_11= 'sideBArtifactType' ( (otherlv_12= RULE_STRING ) ) otherlv_13= 'defaultOrderType' ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) ) otherlv_15= 'multiplicity' ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) ) otherlv_17= '}' ) )
            // InternalOseeDsl.g:1923:1: (otherlv_0= 'relationType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) otherlv_5= 'sideAName' ( (lv_sideAName_6_0= RULE_STRING ) ) otherlv_7= 'sideAArtifactType' ( (otherlv_8= RULE_STRING ) ) otherlv_9= 'sideBName' ( (lv_sideBName_10_0= RULE_STRING ) ) otherlv_11= 'sideBArtifactType' ( (otherlv_12= RULE_STRING ) ) otherlv_13= 'defaultOrderType' ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) ) otherlv_15= 'multiplicity' ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) ) otherlv_17= '}' )
            {
            // InternalOseeDsl.g:1923:1: (otherlv_0= 'relationType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) otherlv_5= 'sideAName' ( (lv_sideAName_6_0= RULE_STRING ) ) otherlv_7= 'sideAArtifactType' ( (otherlv_8= RULE_STRING ) ) otherlv_9= 'sideBName' ( (lv_sideBName_10_0= RULE_STRING ) ) otherlv_11= 'sideBArtifactType' ( (otherlv_12= RULE_STRING ) ) otherlv_13= 'defaultOrderType' ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) ) otherlv_15= 'multiplicity' ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) ) otherlv_17= '}' )
            // InternalOseeDsl.g:1923:3: otherlv_0= 'relationType' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' otherlv_3= 'id' ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) ) otherlv_5= 'sideAName' ( (lv_sideAName_6_0= RULE_STRING ) ) otherlv_7= 'sideAArtifactType' ( (otherlv_8= RULE_STRING ) ) otherlv_9= 'sideBName' ( (lv_sideBName_10_0= RULE_STRING ) ) otherlv_11= 'sideBArtifactType' ( (otherlv_12= RULE_STRING ) ) otherlv_13= 'defaultOrderType' ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) ) otherlv_15= 'multiplicity' ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) ) otherlv_17= '}'
            {
            otherlv_0=(Token)match(input,60,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXRelationTypeAccess().getRelationTypeKeyword_0());
                
            // InternalOseeDsl.g:1927:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:1928:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:1928:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:1929:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_19); 

            			newLeafNode(lv_name_1_0, grammarAccess.getXRelationTypeAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_12); 

                	newLeafNode(otherlv_2, grammarAccess.getXRelationTypeAccess().getLeftCurlyBracketKeyword_2());
                
            otherlv_3=(Token)match(input,19,FOLLOW_13); 

                	newLeafNode(otherlv_3, grammarAccess.getXRelationTypeAccess().getIdKeyword_3());
                
            // InternalOseeDsl.g:1953:1: ( (lv_id_4_0= RULE_WHOLE_NUM_STR ) )
            // InternalOseeDsl.g:1954:1: (lv_id_4_0= RULE_WHOLE_NUM_STR )
            {
            // InternalOseeDsl.g:1954:1: (lv_id_4_0= RULE_WHOLE_NUM_STR )
            // InternalOseeDsl.g:1955:3: lv_id_4_0= RULE_WHOLE_NUM_STR
            {
            lv_id_4_0=(Token)match(input,RULE_WHOLE_NUM_STR,FOLLOW_36); 

            			newLeafNode(lv_id_4_0, grammarAccess.getXRelationTypeAccess().getIdWHOLE_NUM_STRTerminalRuleCall_4_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"id",
                    		lv_id_4_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.WHOLE_NUM_STR");
            	    

            }


            }

            otherlv_5=(Token)match(input,61,FOLLOW_6); 

                	newLeafNode(otherlv_5, grammarAccess.getXRelationTypeAccess().getSideANameKeyword_5());
                
            // InternalOseeDsl.g:1975:1: ( (lv_sideAName_6_0= RULE_STRING ) )
            // InternalOseeDsl.g:1976:1: (lv_sideAName_6_0= RULE_STRING )
            {
            // InternalOseeDsl.g:1976:1: (lv_sideAName_6_0= RULE_STRING )
            // InternalOseeDsl.g:1977:3: lv_sideAName_6_0= RULE_STRING
            {
            lv_sideAName_6_0=(Token)match(input,RULE_STRING,FOLLOW_37); 

            			newLeafNode(lv_sideAName_6_0, grammarAccess.getXRelationTypeAccess().getSideANameSTRINGTerminalRuleCall_6_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"sideAName",
                    		lv_sideAName_6_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_7=(Token)match(input,62,FOLLOW_6); 

                	newLeafNode(otherlv_7, grammarAccess.getXRelationTypeAccess().getSideAArtifactTypeKeyword_7());
                
            // InternalOseeDsl.g:1997:1: ( (otherlv_8= RULE_STRING ) )
            // InternalOseeDsl.g:1998:1: (otherlv_8= RULE_STRING )
            {
            // InternalOseeDsl.g:1998:1: (otherlv_8= RULE_STRING )
            // InternalOseeDsl.g:1999:3: otherlv_8= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                    
            otherlv_8=(Token)match(input,RULE_STRING,FOLLOW_38); 

            		newLeafNode(otherlv_8, grammarAccess.getXRelationTypeAccess().getSideAArtifactTypeXArtifactTypeCrossReference_8_0()); 
            	

            }


            }

            otherlv_9=(Token)match(input,63,FOLLOW_6); 

                	newLeafNode(otherlv_9, grammarAccess.getXRelationTypeAccess().getSideBNameKeyword_9());
                
            // InternalOseeDsl.g:2014:1: ( (lv_sideBName_10_0= RULE_STRING ) )
            // InternalOseeDsl.g:2015:1: (lv_sideBName_10_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2015:1: (lv_sideBName_10_0= RULE_STRING )
            // InternalOseeDsl.g:2016:3: lv_sideBName_10_0= RULE_STRING
            {
            lv_sideBName_10_0=(Token)match(input,RULE_STRING,FOLLOW_39); 

            			newLeafNode(lv_sideBName_10_0, grammarAccess.getXRelationTypeAccess().getSideBNameSTRINGTerminalRuleCall_10_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"sideBName",
                    		lv_sideBName_10_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_11=(Token)match(input,64,FOLLOW_6); 

                	newLeafNode(otherlv_11, grammarAccess.getXRelationTypeAccess().getSideBArtifactTypeKeyword_11());
                
            // InternalOseeDsl.g:2036:1: ( (otherlv_12= RULE_STRING ) )
            // InternalOseeDsl.g:2037:1: (otherlv_12= RULE_STRING )
            {
            // InternalOseeDsl.g:2037:1: (otherlv_12= RULE_STRING )
            // InternalOseeDsl.g:2038:3: otherlv_12= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getXRelationTypeRule());
            	        }
                    
            otherlv_12=(Token)match(input,RULE_STRING,FOLLOW_40); 

            		newLeafNode(otherlv_12, grammarAccess.getXRelationTypeAccess().getSideBArtifactTypeXArtifactTypeCrossReference_12_0()); 
            	

            }


            }

            otherlv_13=(Token)match(input,65,FOLLOW_41); 

                	newLeafNode(otherlv_13, grammarAccess.getXRelationTypeAccess().getDefaultOrderTypeKeyword_13());
                
            // InternalOseeDsl.g:2053:1: ( (lv_defaultOrderType_14_0= ruleRelationOrderType ) )
            // InternalOseeDsl.g:2054:1: (lv_defaultOrderType_14_0= ruleRelationOrderType )
            {
            // InternalOseeDsl.g:2054:1: (lv_defaultOrderType_14_0= ruleRelationOrderType )
            // InternalOseeDsl.g:2055:3: lv_defaultOrderType_14_0= ruleRelationOrderType
            {
             
            	        newCompositeNode(grammarAccess.getXRelationTypeAccess().getDefaultOrderTypeRelationOrderTypeParserRuleCall_14_0()); 
            	    
            pushFollow(FOLLOW_42);
            lv_defaultOrderType_14_0=ruleRelationOrderType();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getXRelationTypeRule());
            	        }
                   		set(
                   			current, 
                   			"defaultOrderType",
                    		lv_defaultOrderType_14_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.RelationOrderType");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_15=(Token)match(input,66,FOLLOW_43); 

                	newLeafNode(otherlv_15, grammarAccess.getXRelationTypeAccess().getMultiplicityKeyword_15());
                
            // InternalOseeDsl.g:2075:1: ( (lv_multiplicity_16_0= ruleRelationMultiplicityEnum ) )
            // InternalOseeDsl.g:2076:1: (lv_multiplicity_16_0= ruleRelationMultiplicityEnum )
            {
            // InternalOseeDsl.g:2076:1: (lv_multiplicity_16_0= ruleRelationMultiplicityEnum )
            // InternalOseeDsl.g:2077:3: lv_multiplicity_16_0= ruleRelationMultiplicityEnum
            {
             
            	        newCompositeNode(grammarAccess.getXRelationTypeAccess().getMultiplicityRelationMultiplicityEnumEnumRuleCall_16_0()); 
            	    
            pushFollow(FOLLOW_44);
            lv_multiplicity_16_0=ruleRelationMultiplicityEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getXRelationTypeRule());
            	        }
                   		set(
                   			current, 
                   			"multiplicity",
                    		lv_multiplicity_16_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.RelationMultiplicityEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_17=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_17, grammarAccess.getXRelationTypeAccess().getRightCurlyBracketKeyword_17());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXRelationType"


    // $ANTLR start "entryRuleRelationOrderType"
    // InternalOseeDsl.g:2105:1: entryRuleRelationOrderType returns [String current=null] : iv_ruleRelationOrderType= ruleRelationOrderType EOF ;
    public final String entryRuleRelationOrderType() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleRelationOrderType = null;


        try {
            // InternalOseeDsl.g:2106:2: (iv_ruleRelationOrderType= ruleRelationOrderType EOF )
            // InternalOseeDsl.g:2107:2: iv_ruleRelationOrderType= ruleRelationOrderType EOF
            {
             newCompositeNode(grammarAccess.getRelationOrderTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationOrderType=ruleRelationOrderType();

            state._fsp--;

             current =iv_ruleRelationOrderType.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationOrderType"


    // $ANTLR start "ruleRelationOrderType"
    // InternalOseeDsl.g:2114:1: ruleRelationOrderType returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'Lexicographical_Ascending' | kw= 'Lexicographical_Descending' | kw= 'Unordered' | this_ID_3= RULE_ID ) ;
    public final AntlrDatatypeRuleToken ruleRelationOrderType() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        Token this_ID_3=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2117:28: ( (kw= 'Lexicographical_Ascending' | kw= 'Lexicographical_Descending' | kw= 'Unordered' | this_ID_3= RULE_ID ) )
            // InternalOseeDsl.g:2118:1: (kw= 'Lexicographical_Ascending' | kw= 'Lexicographical_Descending' | kw= 'Unordered' | this_ID_3= RULE_ID )
            {
            // InternalOseeDsl.g:2118:1: (kw= 'Lexicographical_Ascending' | kw= 'Lexicographical_Descending' | kw= 'Unordered' | this_ID_3= RULE_ID )
            int alt30=4;
            switch ( input.LA(1) ) {
            case 67:
                {
                alt30=1;
                }
                break;
            case 68:
                {
                alt30=2;
                }
                break;
            case 69:
                {
                alt30=3;
                }
                break;
            case RULE_ID:
                {
                alt30=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // InternalOseeDsl.g:2119:2: kw= 'Lexicographical_Ascending'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getRelationOrderTypeAccess().getLexicographical_AscendingKeyword_0()); 
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:2126:2: kw= 'Lexicographical_Descending'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getRelationOrderTypeAccess().getLexicographical_DescendingKeyword_1()); 
                        

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:2133:2: kw= 'Unordered'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getRelationOrderTypeAccess().getUnorderedKeyword_2()); 
                        

                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:2139:10: this_ID_3= RULE_ID
                    {
                    this_ID_3=(Token)match(input,RULE_ID,FOLLOW_2); 

                    		current.merge(this_ID_3);
                        
                     
                        newLeafNode(this_ID_3, grammarAccess.getRelationOrderTypeAccess().getIDTerminalRuleCall_3()); 
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationOrderType"


    // $ANTLR start "entryRuleCondition"
    // InternalOseeDsl.g:2156:1: entryRuleCondition returns [EObject current=null] : iv_ruleCondition= ruleCondition EOF ;
    public final EObject entryRuleCondition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCondition = null;


        try {
            // InternalOseeDsl.g:2157:2: (iv_ruleCondition= ruleCondition EOF )
            // InternalOseeDsl.g:2158:2: iv_ruleCondition= ruleCondition EOF
            {
             newCompositeNode(grammarAccess.getConditionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCondition=ruleCondition();

            state._fsp--;

             current =iv_ruleCondition; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCondition"


    // $ANTLR start "ruleCondition"
    // InternalOseeDsl.g:2165:1: ruleCondition returns [EObject current=null] : (this_SimpleCondition_0= ruleSimpleCondition | this_CompoundCondition_1= ruleCompoundCondition ) ;
    public final EObject ruleCondition() throws RecognitionException {
        EObject current = null;

        EObject this_SimpleCondition_0 = null;

        EObject this_CompoundCondition_1 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2168:28: ( (this_SimpleCondition_0= ruleSimpleCondition | this_CompoundCondition_1= ruleCompoundCondition ) )
            // InternalOseeDsl.g:2169:1: (this_SimpleCondition_0= ruleSimpleCondition | this_CompoundCondition_1= ruleCompoundCondition )
            {
            // InternalOseeDsl.g:2169:1: (this_SimpleCondition_0= ruleSimpleCondition | this_CompoundCondition_1= ruleCompoundCondition )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==22||(LA31_0>=90 && LA31_0<=92)) ) {
                alt31=1;
            }
            else if ( (LA31_0==70) ) {
                alt31=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // InternalOseeDsl.g:2170:5: this_SimpleCondition_0= ruleSimpleCondition
                    {
                     
                            newCompositeNode(grammarAccess.getConditionAccess().getSimpleConditionParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_SimpleCondition_0=ruleSimpleCondition();

                    state._fsp--;

                     
                            current = this_SimpleCondition_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:2180:5: this_CompoundCondition_1= ruleCompoundCondition
                    {
                     
                            newCompositeNode(grammarAccess.getConditionAccess().getCompoundConditionParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_CompoundCondition_1=ruleCompoundCondition();

                    state._fsp--;

                     
                            current = this_CompoundCondition_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCondition"


    // $ANTLR start "entryRuleSimpleCondition"
    // InternalOseeDsl.g:2196:1: entryRuleSimpleCondition returns [EObject current=null] : iv_ruleSimpleCondition= ruleSimpleCondition EOF ;
    public final EObject entryRuleSimpleCondition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSimpleCondition = null;


        try {
            // InternalOseeDsl.g:2197:2: (iv_ruleSimpleCondition= ruleSimpleCondition EOF )
            // InternalOseeDsl.g:2198:2: iv_ruleSimpleCondition= ruleSimpleCondition EOF
            {
             newCompositeNode(grammarAccess.getSimpleConditionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSimpleCondition=ruleSimpleCondition();

            state._fsp--;

             current =iv_ruleSimpleCondition; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSimpleCondition"


    // $ANTLR start "ruleSimpleCondition"
    // InternalOseeDsl.g:2205:1: ruleSimpleCondition returns [EObject current=null] : ( ( (lv_field_0_0= ruleMatchField ) ) ( (lv_op_1_0= ruleCompareOp ) ) ( (lv_expression_2_0= RULE_STRING ) ) ) ;
    public final EObject ruleSimpleCondition() throws RecognitionException {
        EObject current = null;

        Token lv_expression_2_0=null;
        Enumerator lv_field_0_0 = null;

        Enumerator lv_op_1_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2208:28: ( ( ( (lv_field_0_0= ruleMatchField ) ) ( (lv_op_1_0= ruleCompareOp ) ) ( (lv_expression_2_0= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:2209:1: ( ( (lv_field_0_0= ruleMatchField ) ) ( (lv_op_1_0= ruleCompareOp ) ) ( (lv_expression_2_0= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:2209:1: ( ( (lv_field_0_0= ruleMatchField ) ) ( (lv_op_1_0= ruleCompareOp ) ) ( (lv_expression_2_0= RULE_STRING ) ) )
            // InternalOseeDsl.g:2209:2: ( (lv_field_0_0= ruleMatchField ) ) ( (lv_op_1_0= ruleCompareOp ) ) ( (lv_expression_2_0= RULE_STRING ) )
            {
            // InternalOseeDsl.g:2209:2: ( (lv_field_0_0= ruleMatchField ) )
            // InternalOseeDsl.g:2210:1: (lv_field_0_0= ruleMatchField )
            {
            // InternalOseeDsl.g:2210:1: (lv_field_0_0= ruleMatchField )
            // InternalOseeDsl.g:2211:3: lv_field_0_0= ruleMatchField
            {
             
            	        newCompositeNode(grammarAccess.getSimpleConditionAccess().getFieldMatchFieldEnumRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_45);
            lv_field_0_0=ruleMatchField();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getSimpleConditionRule());
            	        }
                   		set(
                   			current, 
                   			"field",
                    		lv_field_0_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.MatchField");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // InternalOseeDsl.g:2227:2: ( (lv_op_1_0= ruleCompareOp ) )
            // InternalOseeDsl.g:2228:1: (lv_op_1_0= ruleCompareOp )
            {
            // InternalOseeDsl.g:2228:1: (lv_op_1_0= ruleCompareOp )
            // InternalOseeDsl.g:2229:3: lv_op_1_0= ruleCompareOp
            {
             
            	        newCompositeNode(grammarAccess.getSimpleConditionAccess().getOpCompareOpEnumRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_6);
            lv_op_1_0=ruleCompareOp();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getSimpleConditionRule());
            	        }
                   		set(
                   			current, 
                   			"op",
                    		lv_op_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.CompareOp");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // InternalOseeDsl.g:2245:2: ( (lv_expression_2_0= RULE_STRING ) )
            // InternalOseeDsl.g:2246:1: (lv_expression_2_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2246:1: (lv_expression_2_0= RULE_STRING )
            // InternalOseeDsl.g:2247:3: lv_expression_2_0= RULE_STRING
            {
            lv_expression_2_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            			newLeafNode(lv_expression_2_0, grammarAccess.getSimpleConditionAccess().getExpressionSTRINGTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getSimpleConditionRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"expression",
                    		lv_expression_2_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSimpleCondition"


    // $ANTLR start "entryRuleCompoundCondition"
    // InternalOseeDsl.g:2271:1: entryRuleCompoundCondition returns [EObject current=null] : iv_ruleCompoundCondition= ruleCompoundCondition EOF ;
    public final EObject entryRuleCompoundCondition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCompoundCondition = null;


        try {
            // InternalOseeDsl.g:2272:2: (iv_ruleCompoundCondition= ruleCompoundCondition EOF )
            // InternalOseeDsl.g:2273:2: iv_ruleCompoundCondition= ruleCompoundCondition EOF
            {
             newCompositeNode(grammarAccess.getCompoundConditionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCompoundCondition=ruleCompoundCondition();

            state._fsp--;

             current =iv_ruleCompoundCondition; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCompoundCondition"


    // $ANTLR start "ruleCompoundCondition"
    // InternalOseeDsl.g:2280:1: ruleCompoundCondition returns [EObject current=null] : (otherlv_0= '(' ( (lv_conditions_1_0= ruleSimpleCondition ) ) ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+ otherlv_4= ')' ) ;
    public final EObject ruleCompoundCondition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_4=null;
        EObject lv_conditions_1_0 = null;

        Enumerator lv_operators_2_0 = null;

        EObject lv_conditions_3_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2283:28: ( (otherlv_0= '(' ( (lv_conditions_1_0= ruleSimpleCondition ) ) ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+ otherlv_4= ')' ) )
            // InternalOseeDsl.g:2284:1: (otherlv_0= '(' ( (lv_conditions_1_0= ruleSimpleCondition ) ) ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+ otherlv_4= ')' )
            {
            // InternalOseeDsl.g:2284:1: (otherlv_0= '(' ( (lv_conditions_1_0= ruleSimpleCondition ) ) ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+ otherlv_4= ')' )
            // InternalOseeDsl.g:2284:3: otherlv_0= '(' ( (lv_conditions_1_0= ruleSimpleCondition ) ) ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+ otherlv_4= ')'
            {
            otherlv_0=(Token)match(input,70,FOLLOW_46); 

                	newLeafNode(otherlv_0, grammarAccess.getCompoundConditionAccess().getLeftParenthesisKeyword_0());
                
            // InternalOseeDsl.g:2288:1: ( (lv_conditions_1_0= ruleSimpleCondition ) )
            // InternalOseeDsl.g:2289:1: (lv_conditions_1_0= ruleSimpleCondition )
            {
            // InternalOseeDsl.g:2289:1: (lv_conditions_1_0= ruleSimpleCondition )
            // InternalOseeDsl.g:2290:3: lv_conditions_1_0= ruleSimpleCondition
            {
             
            	        newCompositeNode(grammarAccess.getCompoundConditionAccess().getConditionsSimpleConditionParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_47);
            lv_conditions_1_0=ruleSimpleCondition();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getCompoundConditionRule());
            	        }
                   		add(
                   			current, 
                   			"conditions",
                    		lv_conditions_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.SimpleCondition");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // InternalOseeDsl.g:2306:2: ( ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) ) )+
            int cnt32=0;
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( ((LA32_0>=88 && LA32_0<=89)) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // InternalOseeDsl.g:2306:3: ( (lv_operators_2_0= ruleXLogicOperator ) ) ( (lv_conditions_3_0= ruleSimpleCondition ) )
            	    {
            	    // InternalOseeDsl.g:2306:3: ( (lv_operators_2_0= ruleXLogicOperator ) )
            	    // InternalOseeDsl.g:2307:1: (lv_operators_2_0= ruleXLogicOperator )
            	    {
            	    // InternalOseeDsl.g:2307:1: (lv_operators_2_0= ruleXLogicOperator )
            	    // InternalOseeDsl.g:2308:3: lv_operators_2_0= ruleXLogicOperator
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getCompoundConditionAccess().getOperatorsXLogicOperatorEnumRuleCall_2_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_46);
            	    lv_operators_2_0=ruleXLogicOperator();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getCompoundConditionRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"operators",
            	            		lv_operators_2_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XLogicOperator");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // InternalOseeDsl.g:2324:2: ( (lv_conditions_3_0= ruleSimpleCondition ) )
            	    // InternalOseeDsl.g:2325:1: (lv_conditions_3_0= ruleSimpleCondition )
            	    {
            	    // InternalOseeDsl.g:2325:1: (lv_conditions_3_0= ruleSimpleCondition )
            	    // InternalOseeDsl.g:2326:3: lv_conditions_3_0= ruleSimpleCondition
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getCompoundConditionAccess().getConditionsSimpleConditionParserRuleCall_2_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_48);
            	    lv_conditions_3_0=ruleSimpleCondition();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getCompoundConditionRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"conditions",
            	            		lv_conditions_3_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.SimpleCondition");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt32 >= 1 ) break loop32;
                        EarlyExitException eee =
                            new EarlyExitException(32, input);
                        throw eee;
                }
                cnt32++;
            } while (true);

            otherlv_4=(Token)match(input,71,FOLLOW_2); 

                	newLeafNode(otherlv_4, grammarAccess.getCompoundConditionAccess().getRightParenthesisKeyword_3());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCompoundCondition"


    // $ANTLR start "entryRuleXArtifactMatcher"
    // InternalOseeDsl.g:2354:1: entryRuleXArtifactMatcher returns [EObject current=null] : iv_ruleXArtifactMatcher= ruleXArtifactMatcher EOF ;
    public final EObject entryRuleXArtifactMatcher() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleXArtifactMatcher = null;


        try {
            // InternalOseeDsl.g:2355:2: (iv_ruleXArtifactMatcher= ruleXArtifactMatcher EOF )
            // InternalOseeDsl.g:2356:2: iv_ruleXArtifactMatcher= ruleXArtifactMatcher EOF
            {
             newCompositeNode(grammarAccess.getXArtifactMatcherRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleXArtifactMatcher=ruleXArtifactMatcher();

            state._fsp--;

             current =iv_ruleXArtifactMatcher; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleXArtifactMatcher"


    // $ANTLR start "ruleXArtifactMatcher"
    // InternalOseeDsl.g:2363:1: ruleXArtifactMatcher returns [EObject current=null] : (otherlv_0= 'artifactMatcher' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= 'where' ( (lv_conditions_3_0= ruleCondition ) ) ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )* otherlv_6= ';' ) ;
    public final EObject ruleXArtifactMatcher() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_6=null;
        EObject lv_conditions_3_0 = null;

        Enumerator lv_operators_4_0 = null;

        EObject lv_conditions_5_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2366:28: ( (otherlv_0= 'artifactMatcher' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= 'where' ( (lv_conditions_3_0= ruleCondition ) ) ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )* otherlv_6= ';' ) )
            // InternalOseeDsl.g:2367:1: (otherlv_0= 'artifactMatcher' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= 'where' ( (lv_conditions_3_0= ruleCondition ) ) ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )* otherlv_6= ';' )
            {
            // InternalOseeDsl.g:2367:1: (otherlv_0= 'artifactMatcher' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= 'where' ( (lv_conditions_3_0= ruleCondition ) ) ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )* otherlv_6= ';' )
            // InternalOseeDsl.g:2367:3: otherlv_0= 'artifactMatcher' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= 'where' ( (lv_conditions_3_0= ruleCondition ) ) ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )* otherlv_6= ';'
            {
            otherlv_0=(Token)match(input,72,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getXArtifactMatcherAccess().getArtifactMatcherKeyword_0());
                
            // InternalOseeDsl.g:2371:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:2372:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2372:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:2373:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_49); 

            			newLeafNode(lv_name_1_0, grammarAccess.getXArtifactMatcherAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getXArtifactMatcherRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_2=(Token)match(input,73,FOLLOW_50); 

                	newLeafNode(otherlv_2, grammarAccess.getXArtifactMatcherAccess().getWhereKeyword_2());
                
            // InternalOseeDsl.g:2393:1: ( (lv_conditions_3_0= ruleCondition ) )
            // InternalOseeDsl.g:2394:1: (lv_conditions_3_0= ruleCondition )
            {
            // InternalOseeDsl.g:2394:1: (lv_conditions_3_0= ruleCondition )
            // InternalOseeDsl.g:2395:3: lv_conditions_3_0= ruleCondition
            {
             
            	        newCompositeNode(grammarAccess.getXArtifactMatcherAccess().getConditionsConditionParserRuleCall_3_0()); 
            	    
            pushFollow(FOLLOW_51);
            lv_conditions_3_0=ruleCondition();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getXArtifactMatcherRule());
            	        }
                   		add(
                   			current, 
                   			"conditions",
                    		lv_conditions_3_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.Condition");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // InternalOseeDsl.g:2411:2: ( ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) ) )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( ((LA33_0>=88 && LA33_0<=89)) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // InternalOseeDsl.g:2411:3: ( (lv_operators_4_0= ruleXLogicOperator ) ) ( (lv_conditions_5_0= ruleCondition ) )
            	    {
            	    // InternalOseeDsl.g:2411:3: ( (lv_operators_4_0= ruleXLogicOperator ) )
            	    // InternalOseeDsl.g:2412:1: (lv_operators_4_0= ruleXLogicOperator )
            	    {
            	    // InternalOseeDsl.g:2412:1: (lv_operators_4_0= ruleXLogicOperator )
            	    // InternalOseeDsl.g:2413:3: lv_operators_4_0= ruleXLogicOperator
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXArtifactMatcherAccess().getOperatorsXLogicOperatorEnumRuleCall_4_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_50);
            	    lv_operators_4_0=ruleXLogicOperator();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXArtifactMatcherRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"operators",
            	            		lv_operators_4_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.XLogicOperator");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // InternalOseeDsl.g:2429:2: ( (lv_conditions_5_0= ruleCondition ) )
            	    // InternalOseeDsl.g:2430:1: (lv_conditions_5_0= ruleCondition )
            	    {
            	    // InternalOseeDsl.g:2430:1: (lv_conditions_5_0= ruleCondition )
            	    // InternalOseeDsl.g:2431:3: lv_conditions_5_0= ruleCondition
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getXArtifactMatcherAccess().getConditionsConditionParserRuleCall_4_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_51);
            	    lv_conditions_5_0=ruleCondition();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getXArtifactMatcherRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"conditions",
            	            		lv_conditions_5_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.Condition");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);

            otherlv_6=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_6, grammarAccess.getXArtifactMatcherAccess().getSemicolonKeyword_5());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXArtifactMatcher"


    // $ANTLR start "entryRuleRole"
    // InternalOseeDsl.g:2459:1: entryRuleRole returns [EObject current=null] : iv_ruleRole= ruleRole EOF ;
    public final EObject entryRuleRole() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRole = null;


        try {
            // InternalOseeDsl.g:2460:2: (iv_ruleRole= ruleRole EOF )
            // InternalOseeDsl.g:2461:2: iv_ruleRole= ruleRole EOF
            {
             newCompositeNode(grammarAccess.getRoleRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRole=ruleRole();

            state._fsp--;

             current =iv_ruleRole; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRole"


    // $ANTLR start "ruleRole"
    // InternalOseeDsl.g:2468:1: ruleRole returns [EObject current=null] : (otherlv_0= 'role' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+ otherlv_7= '}' ) ;
    public final EObject ruleRole() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_7=null;
        EObject lv_usersAndGroups_5_0 = null;

        EObject lv_referencedContexts_6_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2471:28: ( (otherlv_0= 'role' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+ otherlv_7= '}' ) )
            // InternalOseeDsl.g:2472:1: (otherlv_0= 'role' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+ otherlv_7= '}' )
            {
            // InternalOseeDsl.g:2472:1: (otherlv_0= 'role' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+ otherlv_7= '}' )
            // InternalOseeDsl.g:2472:3: otherlv_0= 'role' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+ otherlv_7= '}'
            {
            otherlv_0=(Token)match(input,75,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getRoleAccess().getRoleKeyword_0());
                
            // InternalOseeDsl.g:2476:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:2477:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2477:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:2478:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_10); 

            			newLeafNode(lv_name_1_0, grammarAccess.getRoleAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getRoleRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:2494:2: (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==16) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // InternalOseeDsl.g:2494:4: otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) )
                    {
                    otherlv_2=(Token)match(input,16,FOLLOW_6); 

                        	newLeafNode(otherlv_2, grammarAccess.getRoleAccess().getExtendsKeyword_2_0());
                        
                    // InternalOseeDsl.g:2498:1: ( (otherlv_3= RULE_STRING ) )
                    // InternalOseeDsl.g:2499:1: (otherlv_3= RULE_STRING )
                    {
                    // InternalOseeDsl.g:2499:1: (otherlv_3= RULE_STRING )
                    // InternalOseeDsl.g:2500:3: otherlv_3= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getRoleRule());
                    	        }
                            
                    otherlv_3=(Token)match(input,RULE_STRING,FOLLOW_19); 

                    		newLeafNode(otherlv_3, grammarAccess.getRoleAccess().getSuperRolesRoleCrossReference_2_1_0()); 
                    	

                    }


                    }


                    }
                    break;

            }

            otherlv_4=(Token)match(input,18,FOLLOW_52); 

                	newLeafNode(otherlv_4, grammarAccess.getRoleAccess().getLeftCurlyBracketKeyword_3());
                
            // InternalOseeDsl.g:2515:1: ( ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) ) | ( (lv_referencedContexts_6_0= ruleReferencedContext ) ) )+
            int cnt35=0;
            loop35:
            do {
                int alt35=3;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==77) ) {
                    alt35=1;
                }
                else if ( (LA35_0==76) ) {
                    alt35=2;
                }


                switch (alt35) {
            	case 1 :
            	    // InternalOseeDsl.g:2515:2: ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) )
            	    {
            	    // InternalOseeDsl.g:2515:2: ( (lv_usersAndGroups_5_0= ruleUsersAndGroups ) )
            	    // InternalOseeDsl.g:2516:1: (lv_usersAndGroups_5_0= ruleUsersAndGroups )
            	    {
            	    // InternalOseeDsl.g:2516:1: (lv_usersAndGroups_5_0= ruleUsersAndGroups )
            	    // InternalOseeDsl.g:2517:3: lv_usersAndGroups_5_0= ruleUsersAndGroups
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getRoleAccess().getUsersAndGroupsUsersAndGroupsParserRuleCall_4_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_53);
            	    lv_usersAndGroups_5_0=ruleUsersAndGroups();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getRoleRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"usersAndGroups",
            	            		lv_usersAndGroups_5_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.UsersAndGroups");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // InternalOseeDsl.g:2534:6: ( (lv_referencedContexts_6_0= ruleReferencedContext ) )
            	    {
            	    // InternalOseeDsl.g:2534:6: ( (lv_referencedContexts_6_0= ruleReferencedContext ) )
            	    // InternalOseeDsl.g:2535:1: (lv_referencedContexts_6_0= ruleReferencedContext )
            	    {
            	    // InternalOseeDsl.g:2535:1: (lv_referencedContexts_6_0= ruleReferencedContext )
            	    // InternalOseeDsl.g:2536:3: lv_referencedContexts_6_0= ruleReferencedContext
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getRoleAccess().getReferencedContextsReferencedContextParserRuleCall_4_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_53);
            	    lv_referencedContexts_6_0=ruleReferencedContext();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getRoleRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"referencedContexts",
            	            		lv_referencedContexts_6_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.ReferencedContext");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt35 >= 1 ) break loop35;
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
            } while (true);

            otherlv_7=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_7, grammarAccess.getRoleAccess().getRightCurlyBracketKeyword_5());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRole"


    // $ANTLR start "entryRuleReferencedContext"
    // InternalOseeDsl.g:2564:1: entryRuleReferencedContext returns [EObject current=null] : iv_ruleReferencedContext= ruleReferencedContext EOF ;
    public final EObject entryRuleReferencedContext() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleReferencedContext = null;


        try {
            // InternalOseeDsl.g:2565:2: (iv_ruleReferencedContext= ruleReferencedContext EOF )
            // InternalOseeDsl.g:2566:2: iv_ruleReferencedContext= ruleReferencedContext EOF
            {
             newCompositeNode(grammarAccess.getReferencedContextRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleReferencedContext=ruleReferencedContext();

            state._fsp--;

             current =iv_ruleReferencedContext; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleReferencedContext"


    // $ANTLR start "ruleReferencedContext"
    // InternalOseeDsl.g:2573:1: ruleReferencedContext returns [EObject current=null] : (otherlv_0= 'accessContext' ( (lv_accessContextRef_1_0= RULE_STRING ) ) otherlv_2= ';' ) ;
    public final EObject ruleReferencedContext() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_accessContextRef_1_0=null;
        Token otherlv_2=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2576:28: ( (otherlv_0= 'accessContext' ( (lv_accessContextRef_1_0= RULE_STRING ) ) otherlv_2= ';' ) )
            // InternalOseeDsl.g:2577:1: (otherlv_0= 'accessContext' ( (lv_accessContextRef_1_0= RULE_STRING ) ) otherlv_2= ';' )
            {
            // InternalOseeDsl.g:2577:1: (otherlv_0= 'accessContext' ( (lv_accessContextRef_1_0= RULE_STRING ) ) otherlv_2= ';' )
            // InternalOseeDsl.g:2577:3: otherlv_0= 'accessContext' ( (lv_accessContextRef_1_0= RULE_STRING ) ) otherlv_2= ';'
            {
            otherlv_0=(Token)match(input,76,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getReferencedContextAccess().getAccessContextKeyword_0());
                
            // InternalOseeDsl.g:2581:1: ( (lv_accessContextRef_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:2582:1: (lv_accessContextRef_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2582:1: (lv_accessContextRef_1_0= RULE_STRING )
            // InternalOseeDsl.g:2583:3: lv_accessContextRef_1_0= RULE_STRING
            {
            lv_accessContextRef_1_0=(Token)match(input,RULE_STRING,FOLLOW_54); 

            			newLeafNode(lv_accessContextRef_1_0, grammarAccess.getReferencedContextAccess().getAccessContextRefSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getReferencedContextRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"accessContextRef",
                    		lv_accessContextRef_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_2=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_2, grammarAccess.getReferencedContextAccess().getSemicolonKeyword_2());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleReferencedContext"


    // $ANTLR start "entryRuleUsersAndGroups"
    // InternalOseeDsl.g:2611:1: entryRuleUsersAndGroups returns [EObject current=null] : iv_ruleUsersAndGroups= ruleUsersAndGroups EOF ;
    public final EObject entryRuleUsersAndGroups() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUsersAndGroups = null;


        try {
            // InternalOseeDsl.g:2612:2: (iv_ruleUsersAndGroups= ruleUsersAndGroups EOF )
            // InternalOseeDsl.g:2613:2: iv_ruleUsersAndGroups= ruleUsersAndGroups EOF
            {
             newCompositeNode(grammarAccess.getUsersAndGroupsRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleUsersAndGroups=ruleUsersAndGroups();

            state._fsp--;

             current =iv_ruleUsersAndGroups; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUsersAndGroups"


    // $ANTLR start "ruleUsersAndGroups"
    // InternalOseeDsl.g:2620:1: ruleUsersAndGroups returns [EObject current=null] : (otherlv_0= 'guid' ( (lv_userOrGroupGuid_1_0= RULE_STRING ) ) otherlv_2= ';' ) ;
    public final EObject ruleUsersAndGroups() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_userOrGroupGuid_1_0=null;
        Token otherlv_2=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2623:28: ( (otherlv_0= 'guid' ( (lv_userOrGroupGuid_1_0= RULE_STRING ) ) otherlv_2= ';' ) )
            // InternalOseeDsl.g:2624:1: (otherlv_0= 'guid' ( (lv_userOrGroupGuid_1_0= RULE_STRING ) ) otherlv_2= ';' )
            {
            // InternalOseeDsl.g:2624:1: (otherlv_0= 'guid' ( (lv_userOrGroupGuid_1_0= RULE_STRING ) ) otherlv_2= ';' )
            // InternalOseeDsl.g:2624:3: otherlv_0= 'guid' ( (lv_userOrGroupGuid_1_0= RULE_STRING ) ) otherlv_2= ';'
            {
            otherlv_0=(Token)match(input,77,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getUsersAndGroupsAccess().getGuidKeyword_0());
                
            // InternalOseeDsl.g:2628:1: ( (lv_userOrGroupGuid_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:2629:1: (lv_userOrGroupGuid_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2629:1: (lv_userOrGroupGuid_1_0= RULE_STRING )
            // InternalOseeDsl.g:2630:3: lv_userOrGroupGuid_1_0= RULE_STRING
            {
            lv_userOrGroupGuid_1_0=(Token)match(input,RULE_STRING,FOLLOW_54); 

            			newLeafNode(lv_userOrGroupGuid_1_0, grammarAccess.getUsersAndGroupsAccess().getUserOrGroupGuidSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getUsersAndGroupsRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"userOrGroupGuid",
                    		lv_userOrGroupGuid_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_2=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_2, grammarAccess.getUsersAndGroupsAccess().getSemicolonKeyword_2());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUsersAndGroups"


    // $ANTLR start "entryRuleAccessContext"
    // InternalOseeDsl.g:2658:1: entryRuleAccessContext returns [EObject current=null] : iv_ruleAccessContext= ruleAccessContext EOF ;
    public final EObject entryRuleAccessContext() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAccessContext = null;


        try {
            // InternalOseeDsl.g:2659:2: (iv_ruleAccessContext= ruleAccessContext EOF )
            // InternalOseeDsl.g:2660:2: iv_ruleAccessContext= ruleAccessContext EOF
            {
             newCompositeNode(grammarAccess.getAccessContextRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAccessContext=ruleAccessContext();

            state._fsp--;

             current =iv_ruleAccessContext; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAccessContext"


    // $ANTLR start "ruleAccessContext"
    // InternalOseeDsl.g:2667:1: ruleAccessContext returns [EObject current=null] : (otherlv_0= 'accessContext' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' otherlv_5= 'guid' ( (lv_guid_6_0= RULE_STRING ) ) otherlv_7= ';' ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+ otherlv_10= '}' ) ;
    public final EObject ruleAccessContext() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token lv_guid_6_0=null;
        Token otherlv_7=null;
        Token otherlv_10=null;
        EObject lv_accessRules_8_0 = null;

        EObject lv_hierarchyRestrictions_9_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2670:28: ( (otherlv_0= 'accessContext' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' otherlv_5= 'guid' ( (lv_guid_6_0= RULE_STRING ) ) otherlv_7= ';' ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+ otherlv_10= '}' ) )
            // InternalOseeDsl.g:2671:1: (otherlv_0= 'accessContext' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' otherlv_5= 'guid' ( (lv_guid_6_0= RULE_STRING ) ) otherlv_7= ';' ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+ otherlv_10= '}' )
            {
            // InternalOseeDsl.g:2671:1: (otherlv_0= 'accessContext' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' otherlv_5= 'guid' ( (lv_guid_6_0= RULE_STRING ) ) otherlv_7= ';' ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+ otherlv_10= '}' )
            // InternalOseeDsl.g:2671:3: otherlv_0= 'accessContext' ( (lv_name_1_0= RULE_STRING ) ) (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )? otherlv_4= '{' otherlv_5= 'guid' ( (lv_guid_6_0= RULE_STRING ) ) otherlv_7= ';' ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+ otherlv_10= '}'
            {
            otherlv_0=(Token)match(input,76,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getAccessContextAccess().getAccessContextKeyword_0());
                
            // InternalOseeDsl.g:2675:1: ( (lv_name_1_0= RULE_STRING ) )
            // InternalOseeDsl.g:2676:1: (lv_name_1_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2676:1: (lv_name_1_0= RULE_STRING )
            // InternalOseeDsl.g:2677:3: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_10); 

            			newLeafNode(lv_name_1_0, grammarAccess.getAccessContextAccess().getNameSTRINGTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getAccessContextRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            // InternalOseeDsl.g:2693:2: (otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) ) )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==16) ) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // InternalOseeDsl.g:2693:4: otherlv_2= 'extends' ( (otherlv_3= RULE_STRING ) )
                    {
                    otherlv_2=(Token)match(input,16,FOLLOW_6); 

                        	newLeafNode(otherlv_2, grammarAccess.getAccessContextAccess().getExtendsKeyword_2_0());
                        
                    // InternalOseeDsl.g:2697:1: ( (otherlv_3= RULE_STRING ) )
                    // InternalOseeDsl.g:2698:1: (otherlv_3= RULE_STRING )
                    {
                    // InternalOseeDsl.g:2698:1: (otherlv_3= RULE_STRING )
                    // InternalOseeDsl.g:2699:3: otherlv_3= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getAccessContextRule());
                    	        }
                            
                    otherlv_3=(Token)match(input,RULE_STRING,FOLLOW_19); 

                    		newLeafNode(otherlv_3, grammarAccess.getAccessContextAccess().getSuperAccessContextsAccessContextCrossReference_2_1_0()); 
                    	

                    }


                    }


                    }
                    break;

            }

            otherlv_4=(Token)match(input,18,FOLLOW_55); 

                	newLeafNode(otherlv_4, grammarAccess.getAccessContextAccess().getLeftCurlyBracketKeyword_3());
                
            otherlv_5=(Token)match(input,77,FOLLOW_6); 

                	newLeafNode(otherlv_5, grammarAccess.getAccessContextAccess().getGuidKeyword_4());
                
            // InternalOseeDsl.g:2718:1: ( (lv_guid_6_0= RULE_STRING ) )
            // InternalOseeDsl.g:2719:1: (lv_guid_6_0= RULE_STRING )
            {
            // InternalOseeDsl.g:2719:1: (lv_guid_6_0= RULE_STRING )
            // InternalOseeDsl.g:2720:3: lv_guid_6_0= RULE_STRING
            {
            lv_guid_6_0=(Token)match(input,RULE_STRING,FOLLOW_54); 

            			newLeafNode(lv_guid_6_0, grammarAccess.getAccessContextAccess().getGuidSTRINGTerminalRuleCall_5_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getAccessContextRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"guid",
                    		lv_guid_6_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.STRING");
            	    

            }


            }

            otherlv_7=(Token)match(input,74,FOLLOW_56); 

                	newLeafNode(otherlv_7, grammarAccess.getAccessContextAccess().getSemicolonKeyword_6());
                
            // InternalOseeDsl.g:2740:1: ( ( (lv_accessRules_8_0= ruleObjectRestriction ) ) | ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) ) )+
            int cnt37=0;
            loop37:
            do {
                int alt37=3;
                int LA37_0 = input.LA(1);

                if ( ((LA37_0>=93 && LA37_0<=94)) ) {
                    alt37=1;
                }
                else if ( (LA37_0==78) ) {
                    alt37=2;
                }


                switch (alt37) {
            	case 1 :
            	    // InternalOseeDsl.g:2740:2: ( (lv_accessRules_8_0= ruleObjectRestriction ) )
            	    {
            	    // InternalOseeDsl.g:2740:2: ( (lv_accessRules_8_0= ruleObjectRestriction ) )
            	    // InternalOseeDsl.g:2741:1: (lv_accessRules_8_0= ruleObjectRestriction )
            	    {
            	    // InternalOseeDsl.g:2741:1: (lv_accessRules_8_0= ruleObjectRestriction )
            	    // InternalOseeDsl.g:2742:3: lv_accessRules_8_0= ruleObjectRestriction
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getAccessContextAccess().getAccessRulesObjectRestrictionParserRuleCall_7_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_57);
            	    lv_accessRules_8_0=ruleObjectRestriction();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getAccessContextRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"accessRules",
            	            		lv_accessRules_8_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.ObjectRestriction");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // InternalOseeDsl.g:2759:6: ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) )
            	    {
            	    // InternalOseeDsl.g:2759:6: ( (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction ) )
            	    // InternalOseeDsl.g:2760:1: (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction )
            	    {
            	    // InternalOseeDsl.g:2760:1: (lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction )
            	    // InternalOseeDsl.g:2761:3: lv_hierarchyRestrictions_9_0= ruleHierarchyRestriction
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getAccessContextAccess().getHierarchyRestrictionsHierarchyRestrictionParserRuleCall_7_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_57);
            	    lv_hierarchyRestrictions_9_0=ruleHierarchyRestriction();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getAccessContextRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"hierarchyRestrictions",
            	            		lv_hierarchyRestrictions_9_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.HierarchyRestriction");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt37 >= 1 ) break loop37;
                        EarlyExitException eee =
                            new EarlyExitException(37, input);
                        throw eee;
                }
                cnt37++;
            } while (true);

            otherlv_10=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_10, grammarAccess.getAccessContextAccess().getRightCurlyBracketKeyword_8());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAccessContext"


    // $ANTLR start "entryRuleHierarchyRestriction"
    // InternalOseeDsl.g:2789:1: entryRuleHierarchyRestriction returns [EObject current=null] : iv_ruleHierarchyRestriction= ruleHierarchyRestriction EOF ;
    public final EObject entryRuleHierarchyRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleHierarchyRestriction = null;


        try {
            // InternalOseeDsl.g:2790:2: (iv_ruleHierarchyRestriction= ruleHierarchyRestriction EOF )
            // InternalOseeDsl.g:2791:2: iv_ruleHierarchyRestriction= ruleHierarchyRestriction EOF
            {
             newCompositeNode(grammarAccess.getHierarchyRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleHierarchyRestriction=ruleHierarchyRestriction();

            state._fsp--;

             current =iv_ruleHierarchyRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleHierarchyRestriction"


    // $ANTLR start "ruleHierarchyRestriction"
    // InternalOseeDsl.g:2798:1: ruleHierarchyRestriction returns [EObject current=null] : (otherlv_0= 'childrenOf' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_accessRules_3_0= ruleObjectRestriction ) )+ otherlv_4= '}' ) ;
    public final EObject ruleHierarchyRestriction() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_accessRules_3_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2801:28: ( (otherlv_0= 'childrenOf' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_accessRules_3_0= ruleObjectRestriction ) )+ otherlv_4= '}' ) )
            // InternalOseeDsl.g:2802:1: (otherlv_0= 'childrenOf' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_accessRules_3_0= ruleObjectRestriction ) )+ otherlv_4= '}' )
            {
            // InternalOseeDsl.g:2802:1: (otherlv_0= 'childrenOf' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_accessRules_3_0= ruleObjectRestriction ) )+ otherlv_4= '}' )
            // InternalOseeDsl.g:2802:3: otherlv_0= 'childrenOf' ( (otherlv_1= RULE_STRING ) ) otherlv_2= '{' ( (lv_accessRules_3_0= ruleObjectRestriction ) )+ otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,78,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getHierarchyRestrictionAccess().getChildrenOfKeyword_0());
                
            // InternalOseeDsl.g:2806:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:2807:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:2807:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:2808:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getHierarchyRestrictionRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_19); 

            		newLeafNode(otherlv_1, grammarAccess.getHierarchyRestrictionAccess().getArtifactMatcherRefXArtifactMatcherCrossReference_1_0()); 
            	

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_58); 

                	newLeafNode(otherlv_2, grammarAccess.getHierarchyRestrictionAccess().getLeftCurlyBracketKeyword_2());
                
            // InternalOseeDsl.g:2823:1: ( (lv_accessRules_3_0= ruleObjectRestriction ) )+
            int cnt38=0;
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( ((LA38_0>=93 && LA38_0<=94)) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // InternalOseeDsl.g:2824:1: (lv_accessRules_3_0= ruleObjectRestriction )
            	    {
            	    // InternalOseeDsl.g:2824:1: (lv_accessRules_3_0= ruleObjectRestriction )
            	    // InternalOseeDsl.g:2825:3: lv_accessRules_3_0= ruleObjectRestriction
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getHierarchyRestrictionAccess().getAccessRulesObjectRestrictionParserRuleCall_3_0()); 
            	    	    
            	    pushFollow(FOLLOW_59);
            	    lv_accessRules_3_0=ruleObjectRestriction();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getHierarchyRestrictionRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"accessRules",
            	            		lv_accessRules_3_0, 
            	            		"org.eclipse.osee.framework.core.dsl.OseeDsl.ObjectRestriction");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt38 >= 1 ) break loop38;
                        EarlyExitException eee =
                            new EarlyExitException(38, input);
                        throw eee;
                }
                cnt38++;
            } while (true);

            otherlv_4=(Token)match(input,20,FOLLOW_2); 

                	newLeafNode(otherlv_4, grammarAccess.getHierarchyRestrictionAccess().getRightCurlyBracketKeyword_4());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleHierarchyRestriction"


    // $ANTLR start "entryRuleRelationTypeArtifactTypePredicate"
    // InternalOseeDsl.g:2853:1: entryRuleRelationTypeArtifactTypePredicate returns [EObject current=null] : iv_ruleRelationTypeArtifactTypePredicate= ruleRelationTypeArtifactTypePredicate EOF ;
    public final EObject entryRuleRelationTypeArtifactTypePredicate() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationTypeArtifactTypePredicate = null;


        try {
            // InternalOseeDsl.g:2854:2: (iv_ruleRelationTypeArtifactTypePredicate= ruleRelationTypeArtifactTypePredicate EOF )
            // InternalOseeDsl.g:2855:2: iv_ruleRelationTypeArtifactTypePredicate= ruleRelationTypeArtifactTypePredicate EOF
            {
             newCompositeNode(grammarAccess.getRelationTypeArtifactTypePredicateRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationTypeArtifactTypePredicate=ruleRelationTypeArtifactTypePredicate();

            state._fsp--;

             current =iv_ruleRelationTypeArtifactTypePredicate; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationTypeArtifactTypePredicate"


    // $ANTLR start "ruleRelationTypeArtifactTypePredicate"
    // InternalOseeDsl.g:2862:1: ruleRelationTypeArtifactTypePredicate returns [EObject current=null] : (otherlv_0= 'artifactType' ( (otherlv_1= RULE_STRING ) ) ) ;
    public final EObject ruleRelationTypeArtifactTypePredicate() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2865:28: ( (otherlv_0= 'artifactType' ( (otherlv_1= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:2866:1: (otherlv_0= 'artifactType' ( (otherlv_1= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:2866:1: (otherlv_0= 'artifactType' ( (otherlv_1= RULE_STRING ) ) )
            // InternalOseeDsl.g:2866:3: otherlv_0= 'artifactType' ( (otherlv_1= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,15,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getRelationTypeArtifactTypePredicateAccess().getArtifactTypeKeyword_0());
                
            // InternalOseeDsl.g:2870:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:2871:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:2871:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:2872:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getRelationTypeArtifactTypePredicateRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		newLeafNode(otherlv_1, grammarAccess.getRelationTypeArtifactTypePredicateAccess().getArtifactTypeRefXArtifactTypeCrossReference_1_0()); 
            	

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationTypeArtifactTypePredicate"


    // $ANTLR start "entryRuleRelationTypeArtifactPredicate"
    // InternalOseeDsl.g:2891:1: entryRuleRelationTypeArtifactPredicate returns [EObject current=null] : iv_ruleRelationTypeArtifactPredicate= ruleRelationTypeArtifactPredicate EOF ;
    public final EObject entryRuleRelationTypeArtifactPredicate() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationTypeArtifactPredicate = null;


        try {
            // InternalOseeDsl.g:2892:2: (iv_ruleRelationTypeArtifactPredicate= ruleRelationTypeArtifactPredicate EOF )
            // InternalOseeDsl.g:2893:2: iv_ruleRelationTypeArtifactPredicate= ruleRelationTypeArtifactPredicate EOF
            {
             newCompositeNode(grammarAccess.getRelationTypeArtifactPredicateRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationTypeArtifactPredicate=ruleRelationTypeArtifactPredicate();

            state._fsp--;

             current =iv_ruleRelationTypeArtifactPredicate; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationTypeArtifactPredicate"


    // $ANTLR start "ruleRelationTypeArtifactPredicate"
    // InternalOseeDsl.g:2900:1: ruleRelationTypeArtifactPredicate returns [EObject current=null] : (otherlv_0= 'artifact' ( (otherlv_1= RULE_STRING ) ) ) ;
    public final EObject ruleRelationTypeArtifactPredicate() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;

         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2903:28: ( (otherlv_0= 'artifact' ( (otherlv_1= RULE_STRING ) ) ) )
            // InternalOseeDsl.g:2904:1: (otherlv_0= 'artifact' ( (otherlv_1= RULE_STRING ) ) )
            {
            // InternalOseeDsl.g:2904:1: (otherlv_0= 'artifact' ( (otherlv_1= RULE_STRING ) ) )
            // InternalOseeDsl.g:2904:3: otherlv_0= 'artifact' ( (otherlv_1= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,79,FOLLOW_6); 

                	newLeafNode(otherlv_0, grammarAccess.getRelationTypeArtifactPredicateAccess().getArtifactKeyword_0());
                
            // InternalOseeDsl.g:2908:1: ( (otherlv_1= RULE_STRING ) )
            // InternalOseeDsl.g:2909:1: (otherlv_1= RULE_STRING )
            {
            // InternalOseeDsl.g:2909:1: (otherlv_1= RULE_STRING )
            // InternalOseeDsl.g:2910:3: otherlv_1= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getRelationTypeArtifactPredicateRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		newLeafNode(otherlv_1, grammarAccess.getRelationTypeArtifactPredicateAccess().getArtifactMatcherRefXArtifactMatcherCrossReference_1_0()); 
            	

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationTypeArtifactPredicate"


    // $ANTLR start "entryRuleRelationTypePredicate"
    // InternalOseeDsl.g:2929:1: entryRuleRelationTypePredicate returns [EObject current=null] : iv_ruleRelationTypePredicate= ruleRelationTypePredicate EOF ;
    public final EObject entryRuleRelationTypePredicate() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationTypePredicate = null;


        try {
            // InternalOseeDsl.g:2930:2: (iv_ruleRelationTypePredicate= ruleRelationTypePredicate EOF )
            // InternalOseeDsl.g:2931:2: iv_ruleRelationTypePredicate= ruleRelationTypePredicate EOF
            {
             newCompositeNode(grammarAccess.getRelationTypePredicateRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationTypePredicate=ruleRelationTypePredicate();

            state._fsp--;

             current =iv_ruleRelationTypePredicate; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationTypePredicate"


    // $ANTLR start "ruleRelationTypePredicate"
    // InternalOseeDsl.g:2938:1: ruleRelationTypePredicate returns [EObject current=null] : (this_RelationTypeArtifactPredicate_0= ruleRelationTypeArtifactPredicate | this_RelationTypeArtifactTypePredicate_1= ruleRelationTypeArtifactTypePredicate ) ;
    public final EObject ruleRelationTypePredicate() throws RecognitionException {
        EObject current = null;

        EObject this_RelationTypeArtifactPredicate_0 = null;

        EObject this_RelationTypeArtifactTypePredicate_1 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2941:28: ( (this_RelationTypeArtifactPredicate_0= ruleRelationTypeArtifactPredicate | this_RelationTypeArtifactTypePredicate_1= ruleRelationTypeArtifactTypePredicate ) )
            // InternalOseeDsl.g:2942:1: (this_RelationTypeArtifactPredicate_0= ruleRelationTypeArtifactPredicate | this_RelationTypeArtifactTypePredicate_1= ruleRelationTypeArtifactTypePredicate )
            {
            // InternalOseeDsl.g:2942:1: (this_RelationTypeArtifactPredicate_0= ruleRelationTypeArtifactPredicate | this_RelationTypeArtifactTypePredicate_1= ruleRelationTypeArtifactTypePredicate )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==79) ) {
                alt39=1;
            }
            else if ( (LA39_0==15) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // InternalOseeDsl.g:2943:5: this_RelationTypeArtifactPredicate_0= ruleRelationTypeArtifactPredicate
                    {
                     
                            newCompositeNode(grammarAccess.getRelationTypePredicateAccess().getRelationTypeArtifactPredicateParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_RelationTypeArtifactPredicate_0=ruleRelationTypeArtifactPredicate();

                    state._fsp--;

                     
                            current = this_RelationTypeArtifactPredicate_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:2953:5: this_RelationTypeArtifactTypePredicate_1= ruleRelationTypeArtifactTypePredicate
                    {
                     
                            newCompositeNode(grammarAccess.getRelationTypePredicateAccess().getRelationTypeArtifactTypePredicateParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_RelationTypeArtifactTypePredicate_1=ruleRelationTypeArtifactTypePredicate();

                    state._fsp--;

                     
                            current = this_RelationTypeArtifactTypePredicate_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationTypePredicate"


    // $ANTLR start "entryRuleObjectRestriction"
    // InternalOseeDsl.g:2969:1: entryRuleObjectRestriction returns [EObject current=null] : iv_ruleObjectRestriction= ruleObjectRestriction EOF ;
    public final EObject entryRuleObjectRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleObjectRestriction = null;


        try {
            // InternalOseeDsl.g:2970:2: (iv_ruleObjectRestriction= ruleObjectRestriction EOF )
            // InternalOseeDsl.g:2971:2: iv_ruleObjectRestriction= ruleObjectRestriction EOF
            {
             newCompositeNode(grammarAccess.getObjectRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleObjectRestriction=ruleObjectRestriction();

            state._fsp--;

             current =iv_ruleObjectRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleObjectRestriction"


    // $ANTLR start "ruleObjectRestriction"
    // InternalOseeDsl.g:2978:1: ruleObjectRestriction returns [EObject current=null] : (this_ArtifactMatchRestriction_0= ruleArtifactMatchRestriction | this_ArtifactTypeRestriction_1= ruleArtifactTypeRestriction | this_RelationTypeRestriction_2= ruleRelationTypeRestriction | this_AttributeTypeRestriction_3= ruleAttributeTypeRestriction ) ;
    public final EObject ruleObjectRestriction() throws RecognitionException {
        EObject current = null;

        EObject this_ArtifactMatchRestriction_0 = null;

        EObject this_ArtifactTypeRestriction_1 = null;

        EObject this_RelationTypeRestriction_2 = null;

        EObject this_AttributeTypeRestriction_3 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:2981:28: ( (this_ArtifactMatchRestriction_0= ruleArtifactMatchRestriction | this_ArtifactTypeRestriction_1= ruleArtifactTypeRestriction | this_RelationTypeRestriction_2= ruleRelationTypeRestriction | this_AttributeTypeRestriction_3= ruleAttributeTypeRestriction ) )
            // InternalOseeDsl.g:2982:1: (this_ArtifactMatchRestriction_0= ruleArtifactMatchRestriction | this_ArtifactTypeRestriction_1= ruleArtifactTypeRestriction | this_RelationTypeRestriction_2= ruleRelationTypeRestriction | this_AttributeTypeRestriction_3= ruleAttributeTypeRestriction )
            {
            // InternalOseeDsl.g:2982:1: (this_ArtifactMatchRestriction_0= ruleArtifactMatchRestriction | this_ArtifactTypeRestriction_1= ruleArtifactTypeRestriction | this_RelationTypeRestriction_2= ruleRelationTypeRestriction | this_AttributeTypeRestriction_3= ruleAttributeTypeRestriction )
            int alt40=4;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==93) ) {
                int LA40_1 = input.LA(2);

                if ( (LA40_1==80) ) {
                    switch ( input.LA(3) ) {
                    case 15:
                        {
                        alt40=2;
                        }
                        break;
                    case 79:
                        {
                        alt40=1;
                        }
                        break;
                    case 23:
                        {
                        alt40=4;
                        }
                        break;
                    case 60:
                        {
                        alt40=3;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 40, 3, input);

                        throw nvae;
                    }

                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA40_0==94) ) {
                int LA40_2 = input.LA(2);

                if ( (LA40_2==80) ) {
                    switch ( input.LA(3) ) {
                    case 15:
                        {
                        alt40=2;
                        }
                        break;
                    case 79:
                        {
                        alt40=1;
                        }
                        break;
                    case 23:
                        {
                        alt40=4;
                        }
                        break;
                    case 60:
                        {
                        alt40=3;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 40, 3, input);

                        throw nvae;
                    }

                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // InternalOseeDsl.g:2983:5: this_ArtifactMatchRestriction_0= ruleArtifactMatchRestriction
                    {
                     
                            newCompositeNode(grammarAccess.getObjectRestrictionAccess().getArtifactMatchRestrictionParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_2);
                    this_ArtifactMatchRestriction_0=ruleArtifactMatchRestriction();

                    state._fsp--;

                     
                            current = this_ArtifactMatchRestriction_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:2993:5: this_ArtifactTypeRestriction_1= ruleArtifactTypeRestriction
                    {
                     
                            newCompositeNode(grammarAccess.getObjectRestrictionAccess().getArtifactTypeRestrictionParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_2);
                    this_ArtifactTypeRestriction_1=ruleArtifactTypeRestriction();

                    state._fsp--;

                     
                            current = this_ArtifactTypeRestriction_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:3003:5: this_RelationTypeRestriction_2= ruleRelationTypeRestriction
                    {
                     
                            newCompositeNode(grammarAccess.getObjectRestrictionAccess().getRelationTypeRestrictionParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_2);
                    this_RelationTypeRestriction_2=ruleRelationTypeRestriction();

                    state._fsp--;

                     
                            current = this_RelationTypeRestriction_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:3013:5: this_AttributeTypeRestriction_3= ruleAttributeTypeRestriction
                    {
                     
                            newCompositeNode(grammarAccess.getObjectRestrictionAccess().getAttributeTypeRestrictionParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_2);
                    this_AttributeTypeRestriction_3=ruleAttributeTypeRestriction();

                    state._fsp--;

                     
                            current = this_AttributeTypeRestriction_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleObjectRestriction"


    // $ANTLR start "entryRuleArtifactMatchRestriction"
    // InternalOseeDsl.g:3029:1: entryRuleArtifactMatchRestriction returns [EObject current=null] : iv_ruleArtifactMatchRestriction= ruleArtifactMatchRestriction EOF ;
    public final EObject entryRuleArtifactMatchRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArtifactMatchRestriction = null;


        try {
            // InternalOseeDsl.g:3030:2: (iv_ruleArtifactMatchRestriction= ruleArtifactMatchRestriction EOF )
            // InternalOseeDsl.g:3031:2: iv_ruleArtifactMatchRestriction= ruleArtifactMatchRestriction EOF
            {
             newCompositeNode(grammarAccess.getArtifactMatchRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleArtifactMatchRestriction=ruleArtifactMatchRestriction();

            state._fsp--;

             current =iv_ruleArtifactMatchRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleArtifactMatchRestriction"


    // $ANTLR start "ruleArtifactMatchRestriction"
    // InternalOseeDsl.g:3038:1: ruleArtifactMatchRestriction returns [EObject current=null] : ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifact' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' ) ;
    public final EObject ruleArtifactMatchRestriction() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Enumerator lv_permission_0_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:3041:28: ( ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifact' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' ) )
            // InternalOseeDsl.g:3042:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifact' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' )
            {
            // InternalOseeDsl.g:3042:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifact' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' )
            // InternalOseeDsl.g:3042:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifact' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';'
            {
            // InternalOseeDsl.g:3042:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) )
            // InternalOseeDsl.g:3043:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            {
            // InternalOseeDsl.g:3043:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            // InternalOseeDsl.g:3044:3: lv_permission_0_0= ruleAccessPermissionEnum
            {
             
            	        newCompositeNode(grammarAccess.getArtifactMatchRestrictionAccess().getPermissionAccessPermissionEnumEnumRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_60);
            lv_permission_0_0=ruleAccessPermissionEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getArtifactMatchRestrictionRule());
            	        }
                   		set(
                   			current, 
                   			"permission",
                    		lv_permission_0_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.AccessPermissionEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_1=(Token)match(input,80,FOLLOW_61); 

                	newLeafNode(otherlv_1, grammarAccess.getArtifactMatchRestrictionAccess().getEditKeyword_1());
                
            otherlv_2=(Token)match(input,79,FOLLOW_6); 

                	newLeafNode(otherlv_2, grammarAccess.getArtifactMatchRestrictionAccess().getArtifactKeyword_2());
                
            // InternalOseeDsl.g:3068:1: ( (otherlv_3= RULE_STRING ) )
            // InternalOseeDsl.g:3069:1: (otherlv_3= RULE_STRING )
            {
            // InternalOseeDsl.g:3069:1: (otherlv_3= RULE_STRING )
            // InternalOseeDsl.g:3070:3: otherlv_3= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getArtifactMatchRestrictionRule());
            	        }
                    
            otherlv_3=(Token)match(input,RULE_STRING,FOLLOW_54); 

            		newLeafNode(otherlv_3, grammarAccess.getArtifactMatchRestrictionAccess().getArtifactMatcherRefXArtifactMatcherCrossReference_3_0()); 
            	

            }


            }

            otherlv_4=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_4, grammarAccess.getArtifactMatchRestrictionAccess().getSemicolonKeyword_4());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleArtifactMatchRestriction"


    // $ANTLR start "entryRuleArtifactTypeRestriction"
    // InternalOseeDsl.g:3093:1: entryRuleArtifactTypeRestriction returns [EObject current=null] : iv_ruleArtifactTypeRestriction= ruleArtifactTypeRestriction EOF ;
    public final EObject entryRuleArtifactTypeRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArtifactTypeRestriction = null;


        try {
            // InternalOseeDsl.g:3094:2: (iv_ruleArtifactTypeRestriction= ruleArtifactTypeRestriction EOF )
            // InternalOseeDsl.g:3095:2: iv_ruleArtifactTypeRestriction= ruleArtifactTypeRestriction EOF
            {
             newCompositeNode(grammarAccess.getArtifactTypeRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleArtifactTypeRestriction=ruleArtifactTypeRestriction();

            state._fsp--;

             current =iv_ruleArtifactTypeRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleArtifactTypeRestriction"


    // $ANTLR start "ruleArtifactTypeRestriction"
    // InternalOseeDsl.g:3102:1: ruleArtifactTypeRestriction returns [EObject current=null] : ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifactType' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' ) ;
    public final EObject ruleArtifactTypeRestriction() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Enumerator lv_permission_0_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:3105:28: ( ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifactType' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' ) )
            // InternalOseeDsl.g:3106:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifactType' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' )
            {
            // InternalOseeDsl.g:3106:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifactType' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';' )
            // InternalOseeDsl.g:3106:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'artifactType' ( (otherlv_3= RULE_STRING ) ) otherlv_4= ';'
            {
            // InternalOseeDsl.g:3106:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) )
            // InternalOseeDsl.g:3107:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            {
            // InternalOseeDsl.g:3107:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            // InternalOseeDsl.g:3108:3: lv_permission_0_0= ruleAccessPermissionEnum
            {
             
            	        newCompositeNode(grammarAccess.getArtifactTypeRestrictionAccess().getPermissionAccessPermissionEnumEnumRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_60);
            lv_permission_0_0=ruleAccessPermissionEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getArtifactTypeRestrictionRule());
            	        }
                   		set(
                   			current, 
                   			"permission",
                    		lv_permission_0_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.AccessPermissionEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_1=(Token)match(input,80,FOLLOW_9); 

                	newLeafNode(otherlv_1, grammarAccess.getArtifactTypeRestrictionAccess().getEditKeyword_1());
                
            otherlv_2=(Token)match(input,15,FOLLOW_6); 

                	newLeafNode(otherlv_2, grammarAccess.getArtifactTypeRestrictionAccess().getArtifactTypeKeyword_2());
                
            // InternalOseeDsl.g:3132:1: ( (otherlv_3= RULE_STRING ) )
            // InternalOseeDsl.g:3133:1: (otherlv_3= RULE_STRING )
            {
            // InternalOseeDsl.g:3133:1: (otherlv_3= RULE_STRING )
            // InternalOseeDsl.g:3134:3: otherlv_3= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getArtifactTypeRestrictionRule());
            	        }
                    
            otherlv_3=(Token)match(input,RULE_STRING,FOLLOW_54); 

            		newLeafNode(otherlv_3, grammarAccess.getArtifactTypeRestrictionAccess().getArtifactTypeRefXArtifactTypeCrossReference_3_0()); 
            	

            }


            }

            otherlv_4=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_4, grammarAccess.getArtifactTypeRestrictionAccess().getSemicolonKeyword_4());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleArtifactTypeRestriction"


    // $ANTLR start "entryRuleAttributeTypeRestriction"
    // InternalOseeDsl.g:3157:1: entryRuleAttributeTypeRestriction returns [EObject current=null] : iv_ruleAttributeTypeRestriction= ruleAttributeTypeRestriction EOF ;
    public final EObject entryRuleAttributeTypeRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeTypeRestriction = null;


        try {
            // InternalOseeDsl.g:3158:2: (iv_ruleAttributeTypeRestriction= ruleAttributeTypeRestriction EOF )
            // InternalOseeDsl.g:3159:2: iv_ruleAttributeTypeRestriction= ruleAttributeTypeRestriction EOF
            {
             newCompositeNode(grammarAccess.getAttributeTypeRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAttributeTypeRestriction=ruleAttributeTypeRestriction();

            state._fsp--;

             current =iv_ruleAttributeTypeRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeTypeRestriction"


    // $ANTLR start "ruleAttributeTypeRestriction"
    // InternalOseeDsl.g:3166:1: ruleAttributeTypeRestriction returns [EObject current=null] : ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'attributeType' ( (otherlv_3= RULE_STRING ) ) (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )? otherlv_7= ';' ) ;
    public final EObject ruleAttributeTypeRestriction() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Enumerator lv_permission_0_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:3169:28: ( ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'attributeType' ( (otherlv_3= RULE_STRING ) ) (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )? otherlv_7= ';' ) )
            // InternalOseeDsl.g:3170:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'attributeType' ( (otherlv_3= RULE_STRING ) ) (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )? otherlv_7= ';' )
            {
            // InternalOseeDsl.g:3170:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'attributeType' ( (otherlv_3= RULE_STRING ) ) (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )? otherlv_7= ';' )
            // InternalOseeDsl.g:3170:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'attributeType' ( (otherlv_3= RULE_STRING ) ) (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )? otherlv_7= ';'
            {
            // InternalOseeDsl.g:3170:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) )
            // InternalOseeDsl.g:3171:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            {
            // InternalOseeDsl.g:3171:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            // InternalOseeDsl.g:3172:3: lv_permission_0_0= ruleAccessPermissionEnum
            {
             
            	        newCompositeNode(grammarAccess.getAttributeTypeRestrictionAccess().getPermissionAccessPermissionEnumEnumRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_60);
            lv_permission_0_0=ruleAccessPermissionEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAttributeTypeRestrictionRule());
            	        }
                   		set(
                   			current, 
                   			"permission",
                    		lv_permission_0_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.AccessPermissionEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_1=(Token)match(input,80,FOLLOW_62); 

                	newLeafNode(otherlv_1, grammarAccess.getAttributeTypeRestrictionAccess().getEditKeyword_1());
                
            otherlv_2=(Token)match(input,23,FOLLOW_6); 

                	newLeafNode(otherlv_2, grammarAccess.getAttributeTypeRestrictionAccess().getAttributeTypeKeyword_2());
                
            // InternalOseeDsl.g:3196:1: ( (otherlv_3= RULE_STRING ) )
            // InternalOseeDsl.g:3197:1: (otherlv_3= RULE_STRING )
            {
            // InternalOseeDsl.g:3197:1: (otherlv_3= RULE_STRING )
            // InternalOseeDsl.g:3198:3: otherlv_3= RULE_STRING
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getAttributeTypeRestrictionRule());
            	        }
                    
            otherlv_3=(Token)match(input,RULE_STRING,FOLLOW_63); 

            		newLeafNode(otherlv_3, grammarAccess.getAttributeTypeRestrictionAccess().getAttributeTypeRefXAttributeTypeCrossReference_3_0()); 
            	

            }


            }

            // InternalOseeDsl.g:3209:2: (otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) ) )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==81) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // InternalOseeDsl.g:3209:4: otherlv_4= 'of' otherlv_5= 'artifactType' ( (otherlv_6= RULE_STRING ) )
                    {
                    otherlv_4=(Token)match(input,81,FOLLOW_9); 

                        	newLeafNode(otherlv_4, grammarAccess.getAttributeTypeRestrictionAccess().getOfKeyword_4_0());
                        
                    otherlv_5=(Token)match(input,15,FOLLOW_6); 

                        	newLeafNode(otherlv_5, grammarAccess.getAttributeTypeRestrictionAccess().getArtifactTypeKeyword_4_1());
                        
                    // InternalOseeDsl.g:3217:1: ( (otherlv_6= RULE_STRING ) )
                    // InternalOseeDsl.g:3218:1: (otherlv_6= RULE_STRING )
                    {
                    // InternalOseeDsl.g:3218:1: (otherlv_6= RULE_STRING )
                    // InternalOseeDsl.g:3219:3: otherlv_6= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getAttributeTypeRestrictionRule());
                    	        }
                            
                    otherlv_6=(Token)match(input,RULE_STRING,FOLLOW_54); 

                    		newLeafNode(otherlv_6, grammarAccess.getAttributeTypeRestrictionAccess().getArtifactTypeRefXArtifactTypeCrossReference_4_2_0()); 
                    	

                    }


                    }


                    }
                    break;

            }

            otherlv_7=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_7, grammarAccess.getAttributeTypeRestrictionAccess().getSemicolonKeyword_5());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeTypeRestriction"


    // $ANTLR start "entryRuleRelationTypeRestriction"
    // InternalOseeDsl.g:3244:1: entryRuleRelationTypeRestriction returns [EObject current=null] : iv_ruleRelationTypeRestriction= ruleRelationTypeRestriction EOF ;
    public final EObject entryRuleRelationTypeRestriction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationTypeRestriction = null;


        try {
            // InternalOseeDsl.g:3245:2: (iv_ruleRelationTypeRestriction= ruleRelationTypeRestriction EOF )
            // InternalOseeDsl.g:3246:2: iv_ruleRelationTypeRestriction= ruleRelationTypeRestriction EOF
            {
             newCompositeNode(grammarAccess.getRelationTypeRestrictionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationTypeRestriction=ruleRelationTypeRestriction();

            state._fsp--;

             current =iv_ruleRelationTypeRestriction; 
            match(input,EOF,FOLLOW_2); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationTypeRestriction"


    // $ANTLR start "ruleRelationTypeRestriction"
    // InternalOseeDsl.g:3253:1: ruleRelationTypeRestriction returns [EObject current=null] : ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'relationType' ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) ) ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) ) ( (lv_predicate_6_0= ruleRelationTypePredicate ) )? otherlv_7= ';' ) ;
    public final EObject ruleRelationTypeRestriction() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_7=null;
        Enumerator lv_permission_0_0 = null;

        Enumerator lv_relationTypeMatch_3_0 = null;

        Enumerator lv_restrictedToSide_5_0 = null;

        EObject lv_predicate_6_0 = null;


         enterRule(); 
            
        try {
            // InternalOseeDsl.g:3256:28: ( ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'relationType' ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) ) ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) ) ( (lv_predicate_6_0= ruleRelationTypePredicate ) )? otherlv_7= ';' ) )
            // InternalOseeDsl.g:3257:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'relationType' ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) ) ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) ) ( (lv_predicate_6_0= ruleRelationTypePredicate ) )? otherlv_7= ';' )
            {
            // InternalOseeDsl.g:3257:1: ( ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'relationType' ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) ) ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) ) ( (lv_predicate_6_0= ruleRelationTypePredicate ) )? otherlv_7= ';' )
            // InternalOseeDsl.g:3257:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) ) otherlv_1= 'edit' otherlv_2= 'relationType' ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) ) ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) ) ( (lv_predicate_6_0= ruleRelationTypePredicate ) )? otherlv_7= ';'
            {
            // InternalOseeDsl.g:3257:2: ( (lv_permission_0_0= ruleAccessPermissionEnum ) )
            // InternalOseeDsl.g:3258:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            {
            // InternalOseeDsl.g:3258:1: (lv_permission_0_0= ruleAccessPermissionEnum )
            // InternalOseeDsl.g:3259:3: lv_permission_0_0= ruleAccessPermissionEnum
            {
             
            	        newCompositeNode(grammarAccess.getRelationTypeRestrictionAccess().getPermissionAccessPermissionEnumEnumRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_60);
            lv_permission_0_0=ruleAccessPermissionEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getRelationTypeRestrictionRule());
            	        }
                   		set(
                   			current, 
                   			"permission",
                    		lv_permission_0_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.AccessPermissionEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_1=(Token)match(input,80,FOLLOW_64); 

                	newLeafNode(otherlv_1, grammarAccess.getRelationTypeRestrictionAccess().getEditKeyword_1());
                
            otherlv_2=(Token)match(input,60,FOLLOW_65); 

                	newLeafNode(otherlv_2, grammarAccess.getRelationTypeRestrictionAccess().getRelationTypeKeyword_2());
                
            // InternalOseeDsl.g:3283:1: ( ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) ) | ( (otherlv_4= RULE_STRING ) ) )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==95) ) {
                alt42=1;
            }
            else if ( (LA42_0==RULE_STRING) ) {
                alt42=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // InternalOseeDsl.g:3283:2: ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) )
                    {
                    // InternalOseeDsl.g:3283:2: ( (lv_relationTypeMatch_3_0= ruleRelationTypeMatch ) )
                    // InternalOseeDsl.g:3284:1: (lv_relationTypeMatch_3_0= ruleRelationTypeMatch )
                    {
                    // InternalOseeDsl.g:3284:1: (lv_relationTypeMatch_3_0= ruleRelationTypeMatch )
                    // InternalOseeDsl.g:3285:3: lv_relationTypeMatch_3_0= ruleRelationTypeMatch
                    {
                     
                    	        newCompositeNode(grammarAccess.getRelationTypeRestrictionAccess().getRelationTypeMatchRelationTypeMatchEnumRuleCall_3_0_0()); 
                    	    
                    pushFollow(FOLLOW_66);
                    lv_relationTypeMatch_3_0=ruleRelationTypeMatch();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getRelationTypeRestrictionRule());
                    	        }
                           		set(
                           			current, 
                           			"relationTypeMatch",
                            		true, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.RelationTypeMatch");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3302:6: ( (otherlv_4= RULE_STRING ) )
                    {
                    // InternalOseeDsl.g:3302:6: ( (otherlv_4= RULE_STRING ) )
                    // InternalOseeDsl.g:3303:1: (otherlv_4= RULE_STRING )
                    {
                    // InternalOseeDsl.g:3303:1: (otherlv_4= RULE_STRING )
                    // InternalOseeDsl.g:3304:3: otherlv_4= RULE_STRING
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getRelationTypeRestrictionRule());
                    	        }
                            
                    otherlv_4=(Token)match(input,RULE_STRING,FOLLOW_66); 

                    		newLeafNode(otherlv_4, grammarAccess.getRelationTypeRestrictionAccess().getRelationTypeRefXRelationTypeCrossReference_3_1_0()); 
                    	

                    }


                    }


                    }
                    break;

            }

            // InternalOseeDsl.g:3315:3: ( (lv_restrictedToSide_5_0= ruleXRelationSideEnum ) )
            // InternalOseeDsl.g:3316:1: (lv_restrictedToSide_5_0= ruleXRelationSideEnum )
            {
            // InternalOseeDsl.g:3316:1: (lv_restrictedToSide_5_0= ruleXRelationSideEnum )
            // InternalOseeDsl.g:3317:3: lv_restrictedToSide_5_0= ruleXRelationSideEnum
            {
             
            	        newCompositeNode(grammarAccess.getRelationTypeRestrictionAccess().getRestrictedToSideXRelationSideEnumEnumRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_67);
            lv_restrictedToSide_5_0=ruleXRelationSideEnum();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getRelationTypeRestrictionRule());
            	        }
                   		set(
                   			current, 
                   			"restrictedToSide",
                    		lv_restrictedToSide_5_0, 
                    		"org.eclipse.osee.framework.core.dsl.OseeDsl.XRelationSideEnum");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // InternalOseeDsl.g:3333:2: ( (lv_predicate_6_0= ruleRelationTypePredicate ) )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==15||LA43_0==79) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // InternalOseeDsl.g:3334:1: (lv_predicate_6_0= ruleRelationTypePredicate )
                    {
                    // InternalOseeDsl.g:3334:1: (lv_predicate_6_0= ruleRelationTypePredicate )
                    // InternalOseeDsl.g:3335:3: lv_predicate_6_0= ruleRelationTypePredicate
                    {
                     
                    	        newCompositeNode(grammarAccess.getRelationTypeRestrictionAccess().getPredicateRelationTypePredicateParserRuleCall_5_0()); 
                    	    
                    pushFollow(FOLLOW_54);
                    lv_predicate_6_0=ruleRelationTypePredicate();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getRelationTypeRestrictionRule());
                    	        }
                           		set(
                           			current, 
                           			"predicate",
                            		lv_predicate_6_0, 
                            		"org.eclipse.osee.framework.core.dsl.OseeDsl.RelationTypePredicate");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }
                    break;

            }

            otherlv_7=(Token)match(input,74,FOLLOW_2); 

                	newLeafNode(otherlv_7, grammarAccess.getRelationTypeRestrictionAccess().getSemicolonKeyword_6());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationTypeRestriction"


    // $ANTLR start "ruleRelationMultiplicityEnum"
    // InternalOseeDsl.g:3363:1: ruleRelationMultiplicityEnum returns [Enumerator current=null] : ( (enumLiteral_0= 'ONE_TO_ONE' ) | (enumLiteral_1= 'ONE_TO_MANY' ) | (enumLiteral_2= 'MANY_TO_ONE' ) | (enumLiteral_3= 'MANY_TO_MANY' ) ) ;
    public final Enumerator ruleRelationMultiplicityEnum() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;
        Token enumLiteral_2=null;
        Token enumLiteral_3=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3365:28: ( ( (enumLiteral_0= 'ONE_TO_ONE' ) | (enumLiteral_1= 'ONE_TO_MANY' ) | (enumLiteral_2= 'MANY_TO_ONE' ) | (enumLiteral_3= 'MANY_TO_MANY' ) ) )
            // InternalOseeDsl.g:3366:1: ( (enumLiteral_0= 'ONE_TO_ONE' ) | (enumLiteral_1= 'ONE_TO_MANY' ) | (enumLiteral_2= 'MANY_TO_ONE' ) | (enumLiteral_3= 'MANY_TO_MANY' ) )
            {
            // InternalOseeDsl.g:3366:1: ( (enumLiteral_0= 'ONE_TO_ONE' ) | (enumLiteral_1= 'ONE_TO_MANY' ) | (enumLiteral_2= 'MANY_TO_ONE' ) | (enumLiteral_3= 'MANY_TO_MANY' ) )
            int alt44=4;
            switch ( input.LA(1) ) {
            case 82:
                {
                alt44=1;
                }
                break;
            case 83:
                {
                alt44=2;
                }
                break;
            case 84:
                {
                alt44=3;
                }
                break;
            case 85:
                {
                alt44=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }

            switch (alt44) {
                case 1 :
                    // InternalOseeDsl.g:3366:2: (enumLiteral_0= 'ONE_TO_ONE' )
                    {
                    // InternalOseeDsl.g:3366:2: (enumLiteral_0= 'ONE_TO_ONE' )
                    // InternalOseeDsl.g:3366:4: enumLiteral_0= 'ONE_TO_ONE'
                    {
                    enumLiteral_0=(Token)match(input,82,FOLLOW_2); 

                            current = grammarAccess.getRelationMultiplicityEnumAccess().getONE_TO_ONEEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getRelationMultiplicityEnumAccess().getONE_TO_ONEEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3372:6: (enumLiteral_1= 'ONE_TO_MANY' )
                    {
                    // InternalOseeDsl.g:3372:6: (enumLiteral_1= 'ONE_TO_MANY' )
                    // InternalOseeDsl.g:3372:8: enumLiteral_1= 'ONE_TO_MANY'
                    {
                    enumLiteral_1=(Token)match(input,83,FOLLOW_2); 

                            current = grammarAccess.getRelationMultiplicityEnumAccess().getONE_TO_MANYEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getRelationMultiplicityEnumAccess().getONE_TO_MANYEnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:3378:6: (enumLiteral_2= 'MANY_TO_ONE' )
                    {
                    // InternalOseeDsl.g:3378:6: (enumLiteral_2= 'MANY_TO_ONE' )
                    // InternalOseeDsl.g:3378:8: enumLiteral_2= 'MANY_TO_ONE'
                    {
                    enumLiteral_2=(Token)match(input,84,FOLLOW_2); 

                            current = grammarAccess.getRelationMultiplicityEnumAccess().getMANY_TO_ONEEnumLiteralDeclaration_2().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_2, grammarAccess.getRelationMultiplicityEnumAccess().getMANY_TO_ONEEnumLiteralDeclaration_2()); 
                        

                    }


                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:3384:6: (enumLiteral_3= 'MANY_TO_MANY' )
                    {
                    // InternalOseeDsl.g:3384:6: (enumLiteral_3= 'MANY_TO_MANY' )
                    // InternalOseeDsl.g:3384:8: enumLiteral_3= 'MANY_TO_MANY'
                    {
                    enumLiteral_3=(Token)match(input,85,FOLLOW_2); 

                            current = grammarAccess.getRelationMultiplicityEnumAccess().getMANY_TO_MANYEnumLiteralDeclaration_3().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_3, grammarAccess.getRelationMultiplicityEnumAccess().getMANY_TO_MANYEnumLiteralDeclaration_3()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationMultiplicityEnum"


    // $ANTLR start "ruleCompareOp"
    // InternalOseeDsl.g:3394:1: ruleCompareOp returns [Enumerator current=null] : ( (enumLiteral_0= 'EQ' ) | (enumLiteral_1= 'LIKE' ) ) ;
    public final Enumerator ruleCompareOp() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3396:28: ( ( (enumLiteral_0= 'EQ' ) | (enumLiteral_1= 'LIKE' ) ) )
            // InternalOseeDsl.g:3397:1: ( (enumLiteral_0= 'EQ' ) | (enumLiteral_1= 'LIKE' ) )
            {
            // InternalOseeDsl.g:3397:1: ( (enumLiteral_0= 'EQ' ) | (enumLiteral_1= 'LIKE' ) )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==86) ) {
                alt45=1;
            }
            else if ( (LA45_0==87) ) {
                alt45=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }
            switch (alt45) {
                case 1 :
                    // InternalOseeDsl.g:3397:2: (enumLiteral_0= 'EQ' )
                    {
                    // InternalOseeDsl.g:3397:2: (enumLiteral_0= 'EQ' )
                    // InternalOseeDsl.g:3397:4: enumLiteral_0= 'EQ'
                    {
                    enumLiteral_0=(Token)match(input,86,FOLLOW_2); 

                            current = grammarAccess.getCompareOpAccess().getEQEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getCompareOpAccess().getEQEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3403:6: (enumLiteral_1= 'LIKE' )
                    {
                    // InternalOseeDsl.g:3403:6: (enumLiteral_1= 'LIKE' )
                    // InternalOseeDsl.g:3403:8: enumLiteral_1= 'LIKE'
                    {
                    enumLiteral_1=(Token)match(input,87,FOLLOW_2); 

                            current = grammarAccess.getCompareOpAccess().getLIKEEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getCompareOpAccess().getLIKEEnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCompareOp"


    // $ANTLR start "ruleXLogicOperator"
    // InternalOseeDsl.g:3413:1: ruleXLogicOperator returns [Enumerator current=null] : ( (enumLiteral_0= 'AND' ) | (enumLiteral_1= 'OR' ) ) ;
    public final Enumerator ruleXLogicOperator() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3415:28: ( ( (enumLiteral_0= 'AND' ) | (enumLiteral_1= 'OR' ) ) )
            // InternalOseeDsl.g:3416:1: ( (enumLiteral_0= 'AND' ) | (enumLiteral_1= 'OR' ) )
            {
            // InternalOseeDsl.g:3416:1: ( (enumLiteral_0= 'AND' ) | (enumLiteral_1= 'OR' ) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==88) ) {
                alt46=1;
            }
            else if ( (LA46_0==89) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // InternalOseeDsl.g:3416:2: (enumLiteral_0= 'AND' )
                    {
                    // InternalOseeDsl.g:3416:2: (enumLiteral_0= 'AND' )
                    // InternalOseeDsl.g:3416:4: enumLiteral_0= 'AND'
                    {
                    enumLiteral_0=(Token)match(input,88,FOLLOW_2); 

                            current = grammarAccess.getXLogicOperatorAccess().getANDEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getXLogicOperatorAccess().getANDEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3422:6: (enumLiteral_1= 'OR' )
                    {
                    // InternalOseeDsl.g:3422:6: (enumLiteral_1= 'OR' )
                    // InternalOseeDsl.g:3422:8: enumLiteral_1= 'OR'
                    {
                    enumLiteral_1=(Token)match(input,89,FOLLOW_2); 

                            current = grammarAccess.getXLogicOperatorAccess().getOREnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getXLogicOperatorAccess().getOREnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXLogicOperator"


    // $ANTLR start "ruleMatchField"
    // InternalOseeDsl.g:3432:1: ruleMatchField returns [Enumerator current=null] : ( (enumLiteral_0= 'artifactName' ) | (enumLiteral_1= 'artifactId' ) | (enumLiteral_2= 'branchName' ) | (enumLiteral_3= 'branchUuid' ) ) ;
    public final Enumerator ruleMatchField() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;
        Token enumLiteral_2=null;
        Token enumLiteral_3=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3434:28: ( ( (enumLiteral_0= 'artifactName' ) | (enumLiteral_1= 'artifactId' ) | (enumLiteral_2= 'branchName' ) | (enumLiteral_3= 'branchUuid' ) ) )
            // InternalOseeDsl.g:3435:1: ( (enumLiteral_0= 'artifactName' ) | (enumLiteral_1= 'artifactId' ) | (enumLiteral_2= 'branchName' ) | (enumLiteral_3= 'branchUuid' ) )
            {
            // InternalOseeDsl.g:3435:1: ( (enumLiteral_0= 'artifactName' ) | (enumLiteral_1= 'artifactId' ) | (enumLiteral_2= 'branchName' ) | (enumLiteral_3= 'branchUuid' ) )
            int alt47=4;
            switch ( input.LA(1) ) {
            case 90:
                {
                alt47=1;
                }
                break;
            case 91:
                {
                alt47=2;
                }
                break;
            case 92:
                {
                alt47=3;
                }
                break;
            case 22:
                {
                alt47=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // InternalOseeDsl.g:3435:2: (enumLiteral_0= 'artifactName' )
                    {
                    // InternalOseeDsl.g:3435:2: (enumLiteral_0= 'artifactName' )
                    // InternalOseeDsl.g:3435:4: enumLiteral_0= 'artifactName'
                    {
                    enumLiteral_0=(Token)match(input,90,FOLLOW_2); 

                            current = grammarAccess.getMatchFieldAccess().getArtifactNameEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getMatchFieldAccess().getArtifactNameEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3441:6: (enumLiteral_1= 'artifactId' )
                    {
                    // InternalOseeDsl.g:3441:6: (enumLiteral_1= 'artifactId' )
                    // InternalOseeDsl.g:3441:8: enumLiteral_1= 'artifactId'
                    {
                    enumLiteral_1=(Token)match(input,91,FOLLOW_2); 

                            current = grammarAccess.getMatchFieldAccess().getArtifactIdEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getMatchFieldAccess().getArtifactIdEnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:3447:6: (enumLiteral_2= 'branchName' )
                    {
                    // InternalOseeDsl.g:3447:6: (enumLiteral_2= 'branchName' )
                    // InternalOseeDsl.g:3447:8: enumLiteral_2= 'branchName'
                    {
                    enumLiteral_2=(Token)match(input,92,FOLLOW_2); 

                            current = grammarAccess.getMatchFieldAccess().getBranchNameEnumLiteralDeclaration_2().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_2, grammarAccess.getMatchFieldAccess().getBranchNameEnumLiteralDeclaration_2()); 
                        

                    }


                    }
                    break;
                case 4 :
                    // InternalOseeDsl.g:3453:6: (enumLiteral_3= 'branchUuid' )
                    {
                    // InternalOseeDsl.g:3453:6: (enumLiteral_3= 'branchUuid' )
                    // InternalOseeDsl.g:3453:8: enumLiteral_3= 'branchUuid'
                    {
                    enumLiteral_3=(Token)match(input,22,FOLLOW_2); 

                            current = grammarAccess.getMatchFieldAccess().getBranchUuidEnumLiteralDeclaration_3().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_3, grammarAccess.getMatchFieldAccess().getBranchUuidEnumLiteralDeclaration_3()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMatchField"


    // $ANTLR start "ruleAccessPermissionEnum"
    // InternalOseeDsl.g:3463:1: ruleAccessPermissionEnum returns [Enumerator current=null] : ( (enumLiteral_0= 'ALLOW' ) | (enumLiteral_1= 'DENY' ) ) ;
    public final Enumerator ruleAccessPermissionEnum() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3465:28: ( ( (enumLiteral_0= 'ALLOW' ) | (enumLiteral_1= 'DENY' ) ) )
            // InternalOseeDsl.g:3466:1: ( (enumLiteral_0= 'ALLOW' ) | (enumLiteral_1= 'DENY' ) )
            {
            // InternalOseeDsl.g:3466:1: ( (enumLiteral_0= 'ALLOW' ) | (enumLiteral_1= 'DENY' ) )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==93) ) {
                alt48=1;
            }
            else if ( (LA48_0==94) ) {
                alt48=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // InternalOseeDsl.g:3466:2: (enumLiteral_0= 'ALLOW' )
                    {
                    // InternalOseeDsl.g:3466:2: (enumLiteral_0= 'ALLOW' )
                    // InternalOseeDsl.g:3466:4: enumLiteral_0= 'ALLOW'
                    {
                    enumLiteral_0=(Token)match(input,93,FOLLOW_2); 

                            current = grammarAccess.getAccessPermissionEnumAccess().getALLOWEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getAccessPermissionEnumAccess().getALLOWEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3472:6: (enumLiteral_1= 'DENY' )
                    {
                    // InternalOseeDsl.g:3472:6: (enumLiteral_1= 'DENY' )
                    // InternalOseeDsl.g:3472:8: enumLiteral_1= 'DENY'
                    {
                    enumLiteral_1=(Token)match(input,94,FOLLOW_2); 

                            current = grammarAccess.getAccessPermissionEnumAccess().getDENYEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getAccessPermissionEnumAccess().getDENYEnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAccessPermissionEnum"


    // $ANTLR start "ruleRelationTypeMatch"
    // InternalOseeDsl.g:3482:1: ruleRelationTypeMatch returns [Enumerator current=null] : (enumLiteral_0= 'ALL' ) ;
    public final Enumerator ruleRelationTypeMatch() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3484:28: ( (enumLiteral_0= 'ALL' ) )
            // InternalOseeDsl.g:3485:1: (enumLiteral_0= 'ALL' )
            {
            // InternalOseeDsl.g:3485:1: (enumLiteral_0= 'ALL' )
            // InternalOseeDsl.g:3485:3: enumLiteral_0= 'ALL'
            {
            enumLiteral_0=(Token)match(input,95,FOLLOW_2); 

                    current = grammarAccess.getRelationTypeMatchAccess().getALLEnumLiteralDeclaration().getEnumLiteral().getInstance();
                    newLeafNode(enumLiteral_0, grammarAccess.getRelationTypeMatchAccess().getALLEnumLiteralDeclaration()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationTypeMatch"


    // $ANTLR start "ruleXRelationSideEnum"
    // InternalOseeDsl.g:3495:1: ruleXRelationSideEnum returns [Enumerator current=null] : ( (enumLiteral_0= 'SIDE_A' ) | (enumLiteral_1= 'SIDE_B' ) | (enumLiteral_2= 'BOTH' ) ) ;
    public final Enumerator ruleXRelationSideEnum() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;
        Token enumLiteral_2=null;

         enterRule(); 
        try {
            // InternalOseeDsl.g:3497:28: ( ( (enumLiteral_0= 'SIDE_A' ) | (enumLiteral_1= 'SIDE_B' ) | (enumLiteral_2= 'BOTH' ) ) )
            // InternalOseeDsl.g:3498:1: ( (enumLiteral_0= 'SIDE_A' ) | (enumLiteral_1= 'SIDE_B' ) | (enumLiteral_2= 'BOTH' ) )
            {
            // InternalOseeDsl.g:3498:1: ( (enumLiteral_0= 'SIDE_A' ) | (enumLiteral_1= 'SIDE_B' ) | (enumLiteral_2= 'BOTH' ) )
            int alt49=3;
            switch ( input.LA(1) ) {
            case 96:
                {
                alt49=1;
                }
                break;
            case 97:
                {
                alt49=2;
                }
                break;
            case 98:
                {
                alt49=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }

            switch (alt49) {
                case 1 :
                    // InternalOseeDsl.g:3498:2: (enumLiteral_0= 'SIDE_A' )
                    {
                    // InternalOseeDsl.g:3498:2: (enumLiteral_0= 'SIDE_A' )
                    // InternalOseeDsl.g:3498:4: enumLiteral_0= 'SIDE_A'
                    {
                    enumLiteral_0=(Token)match(input,96,FOLLOW_2); 

                            current = grammarAccess.getXRelationSideEnumAccess().getSIDE_AEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_0, grammarAccess.getXRelationSideEnumAccess().getSIDE_AEnumLiteralDeclaration_0()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // InternalOseeDsl.g:3504:6: (enumLiteral_1= 'SIDE_B' )
                    {
                    // InternalOseeDsl.g:3504:6: (enumLiteral_1= 'SIDE_B' )
                    // InternalOseeDsl.g:3504:8: enumLiteral_1= 'SIDE_B'
                    {
                    enumLiteral_1=(Token)match(input,97,FOLLOW_2); 

                            current = grammarAccess.getXRelationSideEnumAccess().getSIDE_BEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_1, grammarAccess.getXRelationSideEnumAccess().getSIDE_BEnumLiteralDeclaration_1()); 
                        

                    }


                    }
                    break;
                case 3 :
                    // InternalOseeDsl.g:3510:6: (enumLiteral_2= 'BOTH' )
                    {
                    // InternalOseeDsl.g:3510:6: (enumLiteral_2= 'BOTH' )
                    // InternalOseeDsl.g:3510:8: enumLiteral_2= 'BOTH'
                    {
                    enumLiteral_2=(Token)match(input,98,FOLLOW_2); 

                            current = grammarAccess.getXRelationSideEnumAccess().getBOTHEnumLiteralDeclaration_2().getEnumLiteral().getInstance();
                            newLeafNode(enumLiteral_2, grammarAccess.getXRelationSideEnumAccess().getBOTHEnumLiteralDeclaration_2()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleXRelationSideEnum"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x144800000080D002L,0x0000000000001900L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x144800000080C002L,0x0000000000001900L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001900L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000050000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0007FFC000000020L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000000001040000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0007FFC00C000020L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000000040000040L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x0000003E80100000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0007FFC100000020L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0010000000100000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0020000400000042L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0020000400000002L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0380000000100000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0300000000100000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0B80000000000000L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0B80000000100000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000000000000020L,0x0000000000000038L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0000000000000000L,0x00000000003C0000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000000L,0x0000000000C00000L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000400000L,0x000000001C000000L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000080L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000400000L,0x000000001C000040L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000000L,0x0000000003000400L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000000L,0x0000000000003000L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000100000L,0x0000000000003000L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000000000000000L,0x0000000060004000L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000000100000L,0x0000000060004000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000000000000L,0x0000000060000000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0x0000000000100000L,0x0000000060000000L});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_63 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_64 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_65 = new BitSet(new long[]{0x0000000000000010L,0x0000000080000000L});
    public static final BitSet FOLLOW_66 = new BitSet(new long[]{0x0000000000000000L,0x0000000700000000L});
    public static final BitSet FOLLOW_67 = new BitSet(new long[]{0x0000000000008000L,0x0000000000008400L});

}
