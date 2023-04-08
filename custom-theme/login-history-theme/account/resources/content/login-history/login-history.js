import * as React from "../../../../common/keycloak/web_modules/react.js";
import { AccountServiceContext } from "../../account-service/AccountServiceContext.js";
import {
    Card,
    CardBody,
    EmptyState,
    EmptyStateBody,
    EmptyStateVariant,
    Grid,
    GridItem,
    Title
} from '../../../../common/keycloak/web_modules/@patternfly/react-core.js';

export class LoginHistory extends React.Component {
    static contextType = AccountServiceContext;

    constructor(props) {
        super(props);
        this.state = {sessionInfo: ''};
    }

    componentDidMount() {
        this.fetchData();
    }


    fetchData() {
        this.context.doGet("/").then(response => {
            const sessionInfo = response.data.attributes.sessionInfo || '';
            this.setState({sessionInfo});
        });
    }


    render() {
        const e = React.createElement;

        const sessionInfoItems = Object.keys(this.state.sessionInfo).map((key, index) =>
            e('div', { key: index, class: 'pf-c-card' }, [
                e('div', { class: 'pf-c-card__header' }, [
                        e('p', { key: 'card-body-p' }, `${this.state.sessionInfo[key]}`)
                ])
            ])
        );

        return e('div', {class: 'pf-c-card'}, [

            e('div', {class: 'pf-c-card__header'}, [
                e(Title, { headingLevel: 'h1' }, `Login History`)
            ]),

            e('div', {class: 'pf-c-card__body'}, [
                e('p', null, `This is the application login history.`),
            ]),

            e('div', {class: 'pf-c-card__body'}, sessionInfoItems)
        ]);
    }
};